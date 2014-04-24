package MR;

import Tools.*;
import Parameter.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Main {
	
  public static void main(String[] args) throws Exception {
	  
	  Configuration conf = new Configuration();
	  conf.addResource(new Path(Parameter.core_site));  //需要使用HDFS的API，所以需要在工程中引用到配置文件core-site.xml
	  String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	  
	  if (otherArgs.length != 2) {
		  System.err.println("Error！");
		  System.exit(2);
	  }
	  
    Job job = new Job(conf, "Adaboost");
    job.setJarByClass(Main.class);
    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);
    
    job.setMapOutputKeyClass(IntWritable.class); //设置Map的输出Key值类型
    job.setMapOutputValueClass(ClassifySet.class);  //设置Map的输出Value值类型，其为自定义类型
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}