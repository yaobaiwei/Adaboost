package Objects;

import java.io.Serializable; 

/*
 * Learners对象
 * 实现了Serializable接口
 * 分类器对象需要在Map中以List的形式传递给Reduce，因此需要被序列化
 * 
 * 此分类器为线性二分的
 * 在leftValue 与 rightValue 之间则返回1，否则为-1
 * 分类器有权重Weight,处值为1/N
 * 
 */
public class Learners implements Serializable{
	private static final long serialVersionUID = 34519862531L;
	private double leftValue;
	private double rightValue;
	private char fid;  		 //判断该分类器是分类Point对象的X坐标还是Y坐标
	private double weight = 0;
	
	public Learners(double left, double right, char c){
		this.leftValue = left;
		this.rightValue = right;
		this.fid = c;
	}
	
	public double getWeight(){
		return this.weight;
	}
	
	public void setWeight( double w){
		this.weight = w;
	}
	
	//分类函数，在参数范围内输出1，否则输出-1
	public double predict(Point p){
		if(fid == 'x'){
			if(leftValue < p.getX() && p.getX() < rightValue){
				return 1.0;
			}else{
				return -1.0;
			}
		}else{
			if(leftValue < p.getY() && p.getY() < rightValue){
				return 1.0;
			}else{
				return -1.0;
			}
		}
	}
	
}
