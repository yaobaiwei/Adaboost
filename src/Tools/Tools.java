package Tools;

import Objects.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * 
 * @author Chen Hongzhi
 * 工具类对象，存放需要用到的静态处理函数
 */
public class Tools {
	
	/*
	 * Info类存放了分类器的相关信息，详见Info
	 * Max方法
	 * input ArrayList<info> 
	 * output Info
	 * 功能：取出Info集中错误率最少的弱分类器
	 */
	public static Info Max(ArrayList<Info> infolist){
		int num = infolist.size();
		double max1 = 0;
		double max2 = 0;
		Info maxinfo = null;
		for(int i=0;i<num;i++){
			Info info = infolist.get(i);
			if(info.getTerr() >= max1){
				if(info.getTerr() == max1){
					if(info.getErr() > max2){
						maxinfo = info;
						max1 = info.getTerr();
						max2 = info.getErr();
					}
				}else{
					max1 = info.getTerr();
					max2 = info.getErr();
					maxinfo = info;
				}
			}
		}
		return maxinfo;
	}
	
	/*
	 * 
	 * @param array
	 * @return
	 * @throws IOException
	 * 将分类器数组序列化，以便传输给Reduce，封装在ClassifySet类中
	 */
	public static String serialize(Learners[] array) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
		ObjectOutputStream oos = new ObjectOutputStream(base64OutputStream);
		oos.writeObject(array);
		oos.flush();
		oos.close();
		return byteArrayOutputStream.toString();
	}

	/*
	 * 
	 * @param stringArray
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * 反序列化，得到原来的分类器
	 */
	public static Learners[] deserialize(String stringArray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringArray.getBytes());
		Base64InputStream base64InputStream = new Base64InputStream(byteArrayInputStream);
		ObjectInputStream iis = new ObjectInputStream(base64InputStream);
		iis.close();
		return (Learners[])iis.readObject();
	}
	
	
	/*
	 * 在Adaboost的并行化算法中，需要对分类器升序排列
	 * 快排实现该功能
	 * 按照Learners的错误率排序
	 */
	public static void sort(Learners [] learners){
		quickSort(learners,0,learners.length-1);
	}
	
	private static void quickSort(Learners[] learners,int left,int right){
		if(left<right){
			int i = left;
			int j = right+1;
			while(true){
				while(i+1<learners.length && learners[++i].getWeight() < learners[left].getWeight());
				while(j-1 > -1 && learners[--j].getWeight() > learners[left].getWeight());
				if(i>=j){ 
					break;
				}
				swap(learners,i,j);
			}
			swap(learners,left,j);
			quickSort(learners,left,j-1);
			quickSort(learners,j+1,right);
		}
	}
	
	private static void swap(Learners[] learners, int i, int j){
		Learners learner;
		learner = learners[i];
		learners[i] = learners[j];
		learners[j] = learner;
	}
	
	/* 
	 * @param s
	 * @return
	 * sign函数
	 * if x < 0 return -1
	 *    x = 0 return 0
	 *    x > 0 return 1
	 */
	public static double sign( double s){
		if(s < 0)
			return -1.0;
		else if(s == 0.0){
			return 0.0;
		}else{
			return 1.0;
		}
	}
}
