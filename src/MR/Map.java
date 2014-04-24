package MR;

import Objects.*;
import Tools.*;
import Parameter.*;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.LineReader;

public class Map extends Mapper<Object, Text,IntWritable,ClassifySet>{
	
	private ArrayList<Point> pointset = new ArrayList<Point>();   //待分类的Point点集
	private ArrayList<Learners> weak_learners = new ArrayList<Learners>();  //弱分类器
	private Learners [] learners = null;   //迭代T次，每次选取出一个弱分类器保留，放入此ArrayList中
	private ArrayList<Double> boundary = new  ArrayList<Double>();   
	private Makelearners make_learners = new Makelearners();  
	
	/*
	 * 
	 * @see org.apache.hadoop.mapreduce.Mapper#setup(org.apache.hadoop.mapreduce.Mapper.Context)
	 * map前需要做的处理：
	 * 根据input至HDFS的文件中的内容，初始化Point集
	 * 文件的格式类型为
	 * X，Y，label
	 * X为该点X轴坐标，Y为该点Y轴坐标，label为该点类型，取值范围{1,-1}
	 */
	protected void setup(Context context) throws IOException ,InterruptedException {

		String src = Parameter.inputfile;
		FileSystem hdfs = FileSystem.get(context.getConfiguration());
		FSDataInputStream dis = new FSDataInputStream(hdfs.open(new Path(src)));  //获得文本流
		LineReader in = new LineReader(dis); 
		
		Text line = new Text();
		//每行存储一个点，逐行扫描
		while(in.readLine(line) > 0){
			String point[] = line.toString().split(",");
			double x = Double.parseDouble(point[0]);
			double y = Double.parseDouble(point[1]);
			int label = Integer.parseInt(point[2]);
			Point p = new Point(x,y,label);
			pointset.add(p);
		}
		in.close();
		dis.close();
		
		//初始化Point的权重，为1/N
		double weight = 1.0/pointset.size();
		for(int i=0 ; i<pointset.size();i++){
			pointset.get(i).setWeight(weight);  
		}
		
		for(int i=0;i<pointset.size();i++){
			boundary.add(new Double(pointset.get(i).getX()));
		}
		
		//根据点集的X坐标，获得一簇弱分类器
		weak_learners = make_learners.make_leaners(boundary, 'x');
		boundary.clear();
		
		for(int i=0;i<pointset.size();i++){
			boundary.add(new Double(pointset.get(i).getY()));
		}
		
		//根据点集的Y坐标，获得另一族弱分类器
		ArrayList<Learners> list = make_learners.make_leaners(boundary, 'y');
		
		for(int i=0;i<list.size();i++){
			weak_learners.add(list.get(i));
		}
	}

public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
	
	//Map 的value只为一个整数T，表示迭代的次数，仅此而已
	int T = Integer.parseInt(value.toString()); //迭代次数T
	learners = new Learners[T]; //获取有效的弱分类器集合的初始化
	
	int ll = 0;
	for(int k = 0 ; k < T ; k++){
		
		ArrayList<Info> infolist = new ArrayList<Info>();
		for(int i=0;i<weak_learners.size();i++){
			Learners learner = weak_learners.get(i);
			double err = 0.0;
			for(int j=0;j<pointset.size();j++){
				double result = learner.predict(pointset.get(j));
				if(result != pointset.get(j).getLabel()){
					err += pointset.get(j).getWeight();  //得到每组弱分类器的错误率
				}
			}
			infolist.add(new Info(Math.abs(0.5-err),err,learner));  //将分类器以Info的形式存入
		}
		
		Info info = Tools.Max(infolist);  //获得当前迭代中得到的最优分类器
		Learners learner = info.getLearn();  //获得此分类器实体
		double err = info.getErr();
		learner.setWeight(Math.log((1-err)/err)/2);   //计算该分类器的权重
		learners[ll++] = learner;
		
		/*
		 * 根据之前的弱分类器对点集的分类结果正确与否，更新其权值
		 * 然后归一化
		 */
		double Z = 0;
		for(int i = 0;i<pointset.size();i++){
			Point p = pointset.get(i);
			double result = learner.predict(p);
			double t = p.getWeight()*Math.exp(-1*learner.getWeight()*p.getLabel()*result);  //更新的公式
			p.setWeight(t);
			Z+=t;  
		}
		
		//权值归一化
		for(int i=0;i<pointset.size();i++){
			Point p =pointset.get(i);
			p.setWeight(p.getWeight()/Z);
		}
		
	}		
}

	//Map函数处理完毕之后，需要的操作放在clean函数中处理
	/*
	 * (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.Mapper#cleanup(org.apache.hadoop.mapreduce.Mapper.Context)
	 * 1.对选出的弱分类器按照其权值升序排序 
	 * 2.对结果序列化，以传递到Reduce中处理
	 */
    
	protected void cleanup(Context context)throws IOException ,InterruptedException {
		Tools.sort(learners);  //升序排序		
		String stringValue = Tools.serialize(learners);
		ClassifySet classify = new ClassifySet(stringValue);
		context.write(new IntWritable(1),classify);
	}

}