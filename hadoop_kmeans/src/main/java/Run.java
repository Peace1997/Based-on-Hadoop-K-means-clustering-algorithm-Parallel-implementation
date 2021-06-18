import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * 1.获取初始聚类质心
 * 2.初始化Configuration ，job
 * 3.装载map、reduce函数实现的类
 * 4.定义输出k/v类型
 * 5.设置输入输出的文件
 *
 */
public class Run {

    private static String FLAG = "KCLUSTER";

    public static void main(String [] args) throws IOException, ClassNotFoundException, InterruptedException {

//        CreateData c = new CreateData();
//        c.createdata();

        Path samplePath = new Path("input/");
        Path CenterPath = new Path("output/center");

        //加载聚类中心文件，获得字符串形式初始质心
        Center center = new Center();
        String centerString = center.loadInitCenter(CenterPath);
        //System.out.println(centerString);
        System.out.println("test1");

        int index = 0;
        while(index<1000){

            Configuration conf = new Configuration();
            conf.set(FLAG,centerString);//将初始簇质心的字符串放到configuration

            //本次迭代的输出路径，也是下一次迭代质心的读取路径
            CenterPath = new Path("output/center"+ index);
            System.out.println("test2");

            //判断输出路径是否存在，如果存在，则删除
            FileSystem hdfs = FileSystem.get(conf);
            if (hdfs.exists(CenterPath)) {
                hdfs.delete(CenterPath, true);
            }
            else{
                System.out.println("test3");
            }


            //初始一个工作任务,装载相应类
            Job job = new Job(conf,"kmeans"+index);
            job.setJarByClass(Run.class); //构建一个mapreduce能执行的job，为了配置mapreduce能够执行map、reduce函数
            job.setMapperClass(KmeansMapper.class);//job设置mapper类
            job.setCombinerClass(KmeansCombiner.class);//job设置Combine类
            job.setReducerClass(KmeansReducer.class);//job设置reducer类

            job.setMapOutputKeyClass(Text.class);//job设置map的key的输出类型，注意：这里的输出key的类型要与map函数key的输出相同
            job.setMapOutputValueClass(Text.class);//job设置map的value的输出类型
            job.setOutputKeyClass(Text.class);//job设置reduce的key的输出
            job.setOutputValueClass(NullWritable.class);//job设置reduce的value的输出类型

            FileInputFormat.addInputPath(job,samplePath);//job设置输入文件
            FileOutputFormat.setOutputPath(job,CenterPath);//job设置输出文件
            job.waitForCompletion(true);//将运行进程及时输出给用户
            System.out.println("test4");

            //自定义counter的大小，如果和质心的数量相等，说明质心已经不会发生变化，程序停止迭代
            long counter = job.getCounters().getGroup("myCounter").findCounter("kmeansCounter").getValue();//要和reducer相对应
            if(counter == Center.k) System.exit(0);//如果相等则正常退出
            //否则重新加载质心
            center = new Center();
            centerString = center.loadCenter(CenterPath);

            index ++;
        }
        System.exit(0);
    }
}