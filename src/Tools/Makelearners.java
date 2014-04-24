package Tools;

import java.util.ArrayList;
import java.util.Arrays;
import Objects.*;

/*
 * Makelearners
 * 根据传入的浮点数数组（产生于训练集）得到初始的弱分类器
 * input Double[]
 * output ArrayList<Learners> 弱分类器的List 
 */
public class Makelearners {
	private static final double MAX = Double.MAX_VALUE-1;
	
	public Makelearners(){
	}
	
	public double[] ToDoubleArray(ArrayList<Double> list){
		int size = list.size();
		double [] array = new double[size];
		for(int i=0;i<size;i++){
			array[i] = list.get(i).doubleValue();
		}
		return array;
	}
	
	
	public ArrayList<Learners> make_leaners(ArrayList<Double> list, char fid){
		
		ArrayList<Learners> learners = new ArrayList<Learners>();
		double [] array = ToDoubleArray(list);  //list.toArray
		Arrays.sort(array);     //将数组升序排序
		double min = array[0];
		double max = array[array.length-1];
		double [] boundary = new double [array.length+2];
		boundary[0] = min;
		boundary[boundary.length-1] = max;
		for(int i=0;i<array.length;i++){
			boundary[i+1] = array[i];
		}
		for(int i=1;i<boundary.length;i++){
			if(boundary[i] == boundary[i-1])
				continue;
			double l = (boundary[i]+boundary[i-1])/2;   //分类的边界
			learners.add(new Learners(l,MAX,fid));    //将每一组初始化的弱分类器添加到分类器集合中
			learners.add(new Learners(-MAX,l,fid));
		}
		return learners;
	}
}
