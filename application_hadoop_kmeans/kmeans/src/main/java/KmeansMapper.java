import java.io.IOException;


import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * 预处理（setup）阶段拿到给定的初始簇质心
 * 对文件中读到的每行"向量"拿出来计算与每个质心之间的距离
 * 保存与输入点有最近距离的簇质心
 * 输出键（key）是离输入样本点最近的簇质心，值（value）是该向量
 */
public class KmeansMapper extends Mapper<Object,Text,Text,Text>{

    double [][] centers = new double[Center.k][];//每个元素代表一个簇质心在d维空间中某一维的坐标
    String centerStrArray[] = null; //每个元素代表一个簇d维坐标的字符串
    private static String FLAG = "KCLUSTER";

    /*
     *预处理：获得初始簇质心
     * 输入：初始簇的质心字符串
     * 输出：获取质心坐标和分解质心坐标
     */
    @Override
    protected void setup(Context context) throws IOException{
        String kmeanS = context.getConfiguration().get(FLAG);//通过context获得conf对象，继而获取质心文件
        centerStrArray = kmeanS.split("\t"); //经loadInitCenter处理后的质心以字符串形式输出，且每个质心之间以tab分隔
                                                    //因此需要根据tab将每个质心拆分，得到所有簇质心组成的字符串数组
        for(int i=0;i<centerStrArray.length;i++){

            String [] segs = centerStrArray[i].split(",");//因为每个质心的形式为"a,b,..."所以需要以","分隔。
                                                                //得到每个质心的维度坐标的字符串数组，有几维就会被分为几个部分
            centers[i] = new double[segs.length];//确定该质心坐标的维数
            for(int j=0;j<segs.length;j++){
                centers[i][j]=Double.parseDouble(segs[j]);//Double.parseDouble把数字类型的字符串，转换成double类型
                                                          //将分隔的各维坐标值从左至右依次赋值

            }
        }
    }
    /*
     * 计算各个样本到各个簇质心的距离，记录样本最近距离的簇质心，修改key为最近的簇质心
     *  map: (K1, V1) → list(K2,V2)
     * 输入：key：样本偏移量 value：向量
     * 输出：key:簇质点  value：原向量
     * 补：Text 是 Hadoop 中实现的用于封装 Java 数据类型的类，能够被串行化从而便于在分布式环境中进行数据交换，你可以将它们分别视为String 的替代品。
     */
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String []segs = line.split(",");//line值形式为"a,b"，按","进行分割
        double[] sample = new double[segs.length];
        for(int i=0;i<segs.length;i++){ //将分割的各维度坐标赋值到数组相应位置
            sample[i] = Double.parseDouble(segs[i]);
        }

        double min = Double.MAX_VALUE; //min记录最小值
        int index = 0; //记录最小距离的簇质心
        //计算每个输入点与簇质心的距离，并找出距离当前点的最近簇质心
        for(int i=0;i<centers.length;i++) {
            double d = distance(sample,centers[i]);
            if(min > d){
                min =d ;
                index =i ;
            }
        }
        //System.out.println("Mapper输出键值对："+ centerStrArray[index]+"  "+line);
        context.write(new Text(centerStrArray[index]),value); //输出<簇质心，向量>这样的二元组，到context中
    }
    /*
     * 计算两个点之间的距离,欧式距离
     * 输入：两点各维坐标组成的数组
     * 输出：两点的距离
     */
    public static  double distance(double [] a,double [] b){
        if(a == null || b == null || a.length != b.length) return Double.MAX_VALUE;
        double dis =0;
        for(int i=0; i<a.length ; i++){
            dis += Math.pow(a[i] - b[i],2); //两点距离的平方
        }
        return Math.sqrt(dis); //dis取根号即为两点距离
    }

}