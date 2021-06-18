import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;


public class KmeansReducer extends Reducer<Text,Text,Text,NullWritable> {

    Counter counter = null;
    /*
     * 重新计算簇质心：
     * 计算每个簇各维度的平均值。将计算好的平均值当作下一个簇中心，并输出
     * 比较新质点与老质点，若小于阙值则将自定义的counter加1
     * reduce (K2, list(V2)) → list(K3, V3)
     * 输入：key：簇的质心，values： 属于该簇的各个向量
     * 输出：key：的簇的质心的向量  values：新NullWirtable
     */
    @Override
    protected  void reduce(Text key,Iterable<Text> values,Context context) throws IOException, InterruptedException {
        int len = key.toString().split(",").length;
        double [] sum = new double[len];
        int Size =0;//记录簇向量总个数
        int size =0;//记录簇中向量局部个数
        int num=0;//记录一行记录被分割成的个数
        //计算对应维度上对应值的和，将其存入sum数组中
        for(Text text: values){
            String[] segs = text.toString().split(",");
            num = segs.length;
            for(int i=0;i<segs.length-1;i++){ //因为此时还包含了局部簇集的向量数故减1
                sum[i] += Double.parseDouble(segs[i]);
            }
            size=Integer.parseInt(segs[num-1]);
            Size +=size;
        }

        //求sum数组中每个维度的平均值，也就是新的质心。
        StringBuffer sb = new StringBuffer();//由StringBuffer保存的新的聚类簇的质心坐标
        for(int i=0;i<sum.length;i++){
            sum[i] /= Size;
            sb.append(sum[i]);
            if(i==0) sb.append(",");
        }

        //判断新的质心与老的质心是否一样
        boolean flag = true;
        String [] centerStrArray = key.toString().split(","); //存放老质心
        for(int i=0;i<centerStrArray.length;i++){
            if(Math.abs(Double.parseDouble(centerStrArray[i]) - sum[i])>0.00000000001){ //新老质心做差
                flag = false;
                break;
            }
        }

        //如果新的质心和老质心一样，那么相应的计算器加一
        if(flag){
            counter = context.getCounter("myCounter","kmeansCounter");//计数器组名称，计数器名称,与主函数相对应
            counter.increment(1l); //增加1次计数
        }
        //System.out.println("Reduce输出键值对："+sb.toString());
        context.write(new Text(sb.toString()),NullWritable.get());//输出新的质心
    }
}