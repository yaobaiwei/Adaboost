package Objects;

/*
 * 待分类的对象：Point
 * 记录其坐标值，类型和权重
 * 权重是在Ababoost算法中需要使用到的
 */
public class Point {
	private double x;
	private double y;
	private int label; //标记
	private double weight = 0;
	
	public Point(double x, double y, int label){
		this.x = x;
		this.y = y;
		this.label = label;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public int getLabel(){
		return this.label;
	}
	
	public double getWeight(){
		return this.weight;
	}
	
	public void setWeight(double weight){
		this.weight = weight;
	}
}
