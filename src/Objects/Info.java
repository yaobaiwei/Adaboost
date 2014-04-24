package Objects;

/**
 * 
 * @author Chen Hongzhi
 * Info类
 * 存放分类器的实体与性能参数
 * Terr err Learner
 * 
 */
public class Info {
	private double terr;
	private double err;
	private Learners learn;
	
	public Info(double terr,double err, Learners learn){
		this.err = err;
		this.terr = terr;
		this.learn = learn;
	}
	
	public double getErr(){
		return this.err;
	}
	public double getTerr(){
		return this.terr;
	}
	
	public Learners getLearn(){
		return this.learn;
	}
}
