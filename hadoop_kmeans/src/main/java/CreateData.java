import java.io.*;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class CreateData {
    public void createdata()throws IOException {
        Configuration conf = new Configuration();
        String filePath = "input/sample";
        Path linkF = new Path(filePath);
        FileSystem fs = linkF.getFileSystem(conf);

        FSDataOutputStream out =fs.create(linkF);

        double a,b;
        Random r = new Random();

        for(int i=0;i<4000000;i++) {
            a= r.nextDouble()*200;
            b= r.nextDouble()*200;
            out.write((a+","+b+"\n").getBytes("UTF-8"));
            out.flush();
        }
        out.close();
    }
}
