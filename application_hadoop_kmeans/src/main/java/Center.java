import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

/*
 *预备阶段（读取簇质心文件）
 *分两种读取：
 * 1.第一种是读取用户给定的初始簇质心文件
 * 2.第二种是第二次读取reduce输出的簇质心文件
 */

public class Center{
    protected  static int k =3 ; //设定质心的个数，每次都输出两个质心

    /*
     * 读取用户保存在hdfs中初始的质心，并返回字符串，质心之间以tab分隔
     * 输入：hdfs中初始质心文件
     * 输出：字符串形式的质心
     */
    public String loadInitCenter (Path path) throws IOException{

        StringBuffer sb = new StringBuffer(); //声明sb，用以将质心以字符串形式输出。
                                              // StringBuffer类用于处理可变的字符串，它提供修改字符串的方法,它是一个类似于String的字符串缓冲区，通过某些方法调用可以改变该序列的长度和内容。
        Configuration conf = new Configuration();
        FileSystem hdfs = FileSystem.get(conf);
        FSDataInputStream dis = hdfs.open(path);//对目标文件加上输入流
        LineReader in = new LineReader(dis,conf);//LineReader读取类，按换行符切分行，返回一行的内容
        Text line = new Text();
        while(in.readLine(line)>0) {//读到一行数据放入line
            sb.append(line.toString().trim());//若有数据则保存进buffer里
            sb.append("\t");////数据用tab分隔
        }

        return sb.toString().trim();  //trim()用以删除头尾空白符的字符串。
    }

    /*
     *从每次迭代的质心文件中读取质心，并返回字符串
     * 输入：hdfs中reduce输出目录
     * 输出：字符串形式质心
     */
    public String loadCenter (Path path) throws IOException{
        StringBuffer sb = new StringBuffer();

        Configuration conf =new Configuration();
        FileSystem hdfs = FileSystem.get(conf);
        FileStatus [] files = hdfs.listStatus(path);//files数组存储reduce输出目录下所有文件
                                                    //FileStatus封装了文件系统中文件和目录的元数据，包括文件路径、文件长度、块大小等
        //拿到redue输出目录下的所有文件，并过滤掉非簇质心所在文件
        for(int i=0;i<files.length;i++){
            Path filePath =files[i].getPath();
            if(!filePath.getName().contains("part")) continue; //直接过滤掉非质心文件
            FSDataInputStream dis = hdfs.open(filePath);
            LineReader in = new LineReader(dis,conf);
            Text line = new Text();
            while(in.readLine(line)>0){
                sb.append(line.toString().trim());
                sb.append("\t");
            }
        }
        return sb.toString().trim();
    }
}
