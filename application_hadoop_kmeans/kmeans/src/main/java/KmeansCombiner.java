import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import javax.security.auth.login.Configuration;

public  class KmeansCombiner extends Reducer<Text,Text,Text,Text>{
    /*
     *将属于相同key值的value进行局部合并，记录各维度求和结果和该簇向量个数
     * combiner (K2, list(V2)) → list(K3, V3)
     * 输入：key：簇的质心 values：属于该簇的各个向量
     * 输出：key：簇的质心 value：属于该簇向量局部合并结果
     */
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        int size = 0;
        int len = key.toString().split(",").length;
        double sum[] =new double[len];

        //计算对应维度上对应值的和，将其存入sum数组中
        for(Text text: values){
            String[] segs = text.toString().split(",");

            if(segs.length!=2) continue;
            for(int i=0;i<segs.length;i++){
                sum[i] += Double.parseDouble(segs[i]);
            }
            size++;
        }


        //记录各维度求和结果和该簇向量个数
        StringBuffer sb = new StringBuffer();//由StringBuffer保存的新的聚类簇的质心坐标和簇中向量个数
        for(int i=0;i<sum.length;i++){
            sb.append(sum[i]);
            sb.append(",");
        }
        //sb.append(",");
        sb.append(size);//保存局部个数，方便于reduce根据总个数求质心
        //以上同reduce前段过程
        //System.out.println("Combiner输出键值对："+ key+"  "+sb.toString());
        context.write(key,new Text(sb.toString()));//此处与reduce不同
    }
}