package Parameter;

public class Parameter {
	public static String core_site = "/opt/hadoop-1.2.1/conf/core-site.xml";  //需要引入该节点的Hadoop环境中配置文件core-site.xml的位置
	public static String inputfile = "hdfs://localhost:9001/user/root/input/2";  //输入数据文件在HDFS上的位置，文件中放入了待分类的点集的X，Y及Label
	//算法迭代的次数也单独存入一个文件当中，作为map的输入value , 输出文件必然也在HDFS上
	//这两个文件的位置需要在运行时以参数的方式提供
}
