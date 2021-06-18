import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;

/*
 * 从省会城市文件select_city.csv，每次根据上一次的定位选出一个城市对应的所有目的城市，
 * 将所有目的城市的经纬度坐标写入city.csv中，然后返回最后定位的位置，方便下一次继续选取。
 * 每选一个城市city.csv文件就更新一次
 * 输入：selcet_city.csv
 * 输出：返回浏览完一个城市后在selcet_city.csv的定位的值count,以及输出该城市对应所有目的城市坐标文件city.csv
 */
public class City {
    public int city(int record) throws IOException {
        //输入文件
        String filepath1 = "input/select_complex.csv";
        File linkF1 = new File(filepath1);
        BufferedReader in = new BufferedReader(new FileReader(linkF1));
        //输出文件
        String filepath2 = "input/city.csv";
        File linkF2 = new File(filepath2);
        BufferedWriter out = new BufferedWriter(new FileWriter(linkF2));

        String line,lineArray[];
        int count=0;//计数,用于定位

        String temp = "110000"; //用于临时记录城市邮编，和下一个城市做比较.赋110000是第一次输入的是北京的邮编，解决第一次无法判断问题

        while ((line=in.readLine())!=null){
            if (count==record){
                lineArray = line.split(",");
                temp=lineArray[0];
                break;
            }
            count++;
        }
        in.close();

        //输入文件
        BufferedReader in2 = new BufferedReader(new FileReader(linkF1));
        count=0;
        while ((line=in2.readLine())!=null){
            if (count>=record){
                lineArray = line.split(",");
                if(temp.equals(lineArray[0])){
                    out.write(lineArray[6]+","+lineArray[7]);
                    out.newLine();
                    out.flush();
                }
               else break;
            }
            count++;
        }
        in2.close();
        out.close();
    return count;//便于下一次的定位
    }

//    //对满足条件的城市进行筛选并写入select_complex.csv
//    public void selectCity() throws  IOException{
//        //输入文件
//        String filepath1 = "input/order_complex.csv";
//        File linkF1 = new File(filepath1);
//        BufferedReader in = new BufferedReader(new FileReader(linkF1));
//        //输出文件
//        String filepath2 = "input/select_complex.csv";
//        File linkF2 = new File(filepath2);
//        BufferedWriter out = new BufferedWriter(new FileWriter(linkF2));
//
//        String line,lineArray[];
//        boolean judge=false;//获取该起始城市出现的次数是否满足要求
//        int count=0;//计数,用于定位
//        String temp2 = "110000"; //用于临时记录城市邮编，和下一个城市做比较.赋110000是第一次输入的是北京的邮编，解决第一次无法判断问题
//
//        while(count<14483) {
//            judge = judegeCity();
//            while ((line = in.readLine()) != null) {
//                lineArray = line.split(",");
//                if (count >= index) {
//                    if (judge == true && temp2.equals(lineArray[0])) {
//                        out.write(lineArray[0] + "," + lineArray[1]+","+ lineArray[2] + "," + lineArray[3]+","+ lineArray[4] + "," + lineArray[5]+","+ lineArray[6] + "," + lineArray[7]);
//                        out.newLine();
//                    }
//                    if(!temp2.equals(lineArray[0]))
//                    {
//                        temp2=lineArray[0];
//                        break;
//                    }
//                }
//                //temp2.equals(lineArray[0]);
//                count++;
//            }
//
//            index=count;
//        }
//        in.close();
//        out.close();
//    }
//    //根据record值定位到order_complex.csvc相应位置,然后选择该行的起始城市,判断该城市出现的次数是否满足要求，
//    //满足返回true，不满足返回false
//    public boolean judegeCity() throws IOException {
//
//        boolean judge=false;
//        //输入文件
//        String filepath = "input/order_complex.csv";
//        File linkF = new File(filepath);
//        BufferedReader in = new BufferedReader(new FileReader(linkF));
//
//        String line,lineArray[];
//        int count1=0;//计数,用于定位
//        int count2=0;//计数，用于判断城市出现次数
//        while ((line = in.readLine())!=null){
//
//            lineArray=line.split(",");
//            if(count1>=index){
//                if(temp.equals(lineArray[0])) count2++;//如果大于定位的位置且城市匹配，起始城市出现次数加一
//                else{
//                    temp = lineArray[0];
//                    break;
//                }
//            }
//            count1++;
//        }
//
//        if(count2>=50) judge=true;
//        in.close();
//        return judge;
//    }

}
