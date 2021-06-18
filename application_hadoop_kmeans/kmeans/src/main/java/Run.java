import java.io.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * 依次迭代起始城市对应的目的城市坐标文件city.csv，然后对其聚类分析，获取起始城市的邮编和名字
 * 并获取最后一次迭代结果的k值，将其写入K_cities文件
 * k_cities.csv文件不用每次迭代都更新
 * 输入：每次输入一个城市对应的目的坐标文件city.csv
 * 输出：输出包含起始城市信息以及最后迭代k值的文件k_cites.csv
 */
public class Run {

    private static int index;//用于记录每一次迭代的次数，方便将最后的k值插入到文件中
    private static String FLAG = "KCLUSTER";
    private static String centerString; //获取最后的迭代k值结果

    public static void main(String [] args) throws IOException, ClassNotFoundException, InterruptedException {

//         AirTicketsWithCoordinate a = new AirTicketsWithCoordinate();
//         a.nation();
        int record=0;//用于记录每次读取order_complex文件的位置
        int index=0;//用于记录每个起始城市在文件中第一次出现所在的位置
        //设置将各城市的k值输入到k_cities文件
        String filepath1 = "input/k_cities.csv";
        File linkF1 = new File(filepath1);
        BufferedWriter out = new BufferedWriter(new FileWriter(linkF1));

        //输入文件
        String filepath2 = "input/select_complex.csv";
        File linkF2 = new File(filepath2);

           while(record<9185) {
                Path samplePath = new Path("input/city.csv");
                Path CenterPath = new Path("output/center");

                String line,lineArray1[];//获取城市编号和名称
                int count=0;//用于计数，以获取城市的邮编和名称
                City c = new City();

                index=record;

                record = c.city(record);

               BufferedReader in = new BufferedReader(new FileReader(linkF2));

               while ((line=in.readLine())!=null){
                   if (count==index){
                       lineArray1 = line.split(",");
                       out.write(lineArray1[0]+","+lineArray1[1]+",");
                       break;
                   }
                   count++;
               }
               in.close();

                run(samplePath, CenterPath);



                //输入获取最后的迭代k值结果，将其保存至k_cities文件中
                String lineArray2[];
                lineArray2 = centerString.split("\t");
                out.write(lineArray2[0] + "," + lineArray2[1] + "," + lineArray2[2]);
                out.newLine();
                out.flush();
            }
            out.close();

        System.exit(0);
    }
//基于hadoop的kmeans运行
/*
 * 1.获取初始聚类质心
 * 2.初始化Configuration ，job
 * 3.装载map、reduce函数实现的类
 * 4.定义输出k/v类型
 * 5.构建输入输出的数
 *
 */
    public static void run(Path samplePath,Path CenterPath) throws IOException, ClassNotFoundException, InterruptedException {

        //加载聚类中心文件，获得字符串形式初始质心
        Center center = new Center();
        centerString = center.loadInitCenter(CenterPath);
        System.out.println(centerString);
        System.out.println("test1");

        index = 0;
        long counter = 0;
        while(counter!= Center.k){

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
            job.setCombinerClass(KmeansCombiner.class);//job设置Mapper类
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
            counter = job.getCounters().getGroup("myCounter").findCounter("kmeansCounter").getValue();//要和reducer相对应
            if(counter == Center.k) break;//如果相等则正常退出
            //否则重新加载质心
            center = new Center();
            centerString = center.loadCenter(CenterPath);

            index ++;
        }


    }
}