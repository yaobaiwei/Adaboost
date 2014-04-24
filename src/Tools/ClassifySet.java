package Tools;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

/*
 * ClassifySet 实现Writable接口
 * 自定义的MapReduce参数类型 
 * 实现将 Map中通过Adaboost算法训练得到的弱分类器集 传递到Reduce中
 * 
 */
public class ClassifySet implements Writable {
	
	private String stringValue;
	
	public ClassifySet(String s){
		this.stringValue = s;
	}
	
	public ClassifySet(){
		this("");
	}
	
	public String getList(){
		return this.stringValue;
	}
	
	public void readFields(DataInput in)throws IOException {
		this.stringValue = in.readUTF();    
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeUTF(stringValue);
	}
}
