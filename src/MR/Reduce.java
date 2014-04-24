package MR;

import Objects.*;
import Parameter.Parameter;
import Tools.*;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.LineReader;

public class Reduce extends Reducer<IntWritable,ClassifySet,Text,IntWritable> {
	
	private ArrayList<ArrayList<Learners>> list = new ArrayList<ArrayList<Learners>>();
	private ArrayList<Learners> temp = new ArrayList<Learners>();
	private ArrayList<Point> pointset = new ArrayList<Point>();
	
	protected void setup(Context context) throws IOException ,InterruptedException {

		//在Reduce中重新获取Point集
		String src = Parameter.inputfile;
		FileSystem hdfs = FileSystem.get(context.getConfiguration());
		FSDataInputStream dis = new FSDataInputStream(hdfs.open(new Path(src)));
		LineReader in = new LineReader(dis);
		
		//过程与Map中获取点集至内存的处理方法相同
		Text line = new Text();
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
		
		//初始化权重
		double weight = 1.0/pointset.size();
		for(int i=0 ; i<pointset.size();i++){
			pointset.get(i).setWeight(weight);  
		}
	}
		
	public void reduce(IntWritable key, Iterable<ClassifySet> values, Context context) throws IOException, InterruptedException {
		try{
			//reduce中得到的Values是所有Map处理后的一个列表，列表中每一项都是一个list of classifier
			for (ClassifySet val : values) {
					Learners[] learners = Tools.deserialize(val.getList());  //反序列化得到分类器集
					temp.clear();
					int length = learners.length;
					for(int i=0;i<length;i++){
						temp.add(learners[i]);
					}
					list.add(temp);  //置入List中
			}			
		}catch(ClassNotFoundException e){
			e.getStackTrace();
		}
}

	/*
	 * cleanup中主要实现对新的分类器的函数拟合
	 */
	protected void cleanup(Context context)throws IOException ,InterruptedException {
		
		for(int k = 0; k < pointset.size(); k++){  //每个点被新的强分类器处理，得出处理结果（分类结果）后,输出到结果文件中
			Point p = pointset.get(k);
			double category = 0.0;
			for(int i = 0;i<list.get(0).size();i++){
				double sum = 0;
				double weight = 0;
				for(int j=0;j<list.size();j++){
					
					//将某处理对象Point被同次序下的弱分类器处理后的结果收集起来，送给sign函数处理
					Learners learn = list.get(j).get(i);  
					sum += learn.predict(p);
					weight += learn.getWeight();
				}
				
				//获取同序列下的分类器在强分类器中所占的权值
				weight = weight / list.size();
				sum = Tools.sign(sum);  //用sign函数拟合此分类器的结果
				category += weight * sum;  //判断分类结果是否正确，正确的结果必为正 因为1*1 = -1 * -1 = 1 >0
			}
			
			//输出到HDFS的结果文件中
			if(category > 0){
				context.write(new Text(Double.toString(p.getX())+"\t,"+Double.toString(p.getY())+"\t,"+Integer.toString(p.getLabel())+"\t"),new IntWritable(1));
			}else{
				context.write(new Text(Double.toString(p.getX())+"\t,"+Double.toString(p.getY())+"\t,"+Integer.toString(p.getLabel())+"\t"),new IntWritable(-1));				
			}
		}
	}
}