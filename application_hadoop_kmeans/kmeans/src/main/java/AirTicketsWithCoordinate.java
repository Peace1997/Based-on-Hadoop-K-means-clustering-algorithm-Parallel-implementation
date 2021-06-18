import jdk.internal.dynalink.support.NameCodec;

import java.io.*;
import java.lang.reflect.Field;

/*
 * 对初始的文件（机票文件和中国省市地区坐标文件）进行预处理
 */

public class AirTicketsWithCoordinate {

    //csv文件的读取和写入
    //将坐标文件中的经纬度写入机票文件中
    /*
     * 给机票文件中的城市加上'市'，个别地区需要单独处理
     * 输入文件：AirTickets.csv
     * 修改后文件：NewAirTickets.csv
     * 
     * 其中黎平市->黎平县，果洛市->果洛藏族自治州，迪庆市->迪庆藏族自治州，西双版纳市->西双版纳傣族自治州，连城市->连城县，荔波市->荔波县
     * 德宏市->德宏傣族景颇族自治州傣族景颇族自治州，阿里市->阿里地区，九寨沟市->九寨沟县，稻城市->稻城县，伊犁市->伊犁哈萨克自治州
     */
    public void addshi()throws IOException{
        //输入文件
        String filePath = "input/initial/AirTickets.csv";
        File linkF =new File(filePath);
        BufferedReader in = new BufferedReader(new FileReader(linkF));
        //输出文件
        File writeName = new File("input/initial/NewAirTickets.csv");
        FileWriter writer = new FileWriter(writeName);
        BufferedWriter out = new BufferedWriter(writer);
        
        String lineArray[],line;
        in.readLine();//读取表头
        while((line=in.readLine())!=null) {//读取每一行文件
            lineArray = line.split(",");
            //System.out.println(lineArray[0] + "市" + "," + lineArray[1] + "市");
            out.write(lineArray[0] + "市" + "," + lineArray[1] + "市");
            out.newLine();
            out.flush();
        }
        in.close();
        out.close();
    }

    /*
     * 读取机票文件中的start_city/end_city属性的每一个值，和坐标文件相匹配，匹配成功，
     * 则将（起始城市,目的城市编号,目的城市，目的城市经度，目的城市纬度）添加到complex.csv中去
     * 输入文件：NewAirTickets.csv,ChinaCoordinates.csv
     * 输出文件：complex.csv
     */
    public void Complex() throws IOException{
        //输入文件
        String filePath1 = "input/initial/NewAirTickets.csv";
        String filePath2 = "input/initial/ChinaCoordinates.csv";
        File linkF1 = new File(filePath1);
        File linkF2 = new File(filePath2);
        BufferedReader in1 = new BufferedReader(new FileReader(linkF1));
        
        //输出文件
        String filePath3 = "input/complex.csv";
        File writeName = new File(filePath3);
        BufferedWriter out = new BufferedWriter(new FileWriter(writeName));

        String lineArray1[],line1,lineArray2[],line2;
        while((line1=in1.readLine())!=null){
            lineArray1 = line1.split(",");
            
            //为了BufferedReader每次重新定位到坐标文件的首部
            BufferedReader in2 = new BufferedReader(new FileReader(linkF2));
            //为complex文件加上（起始城市的城市编号,起始城市名称，起始城市经度，起始城市维度）
            while ((line2 = in2.readLine())!=null){
                lineArray2 = line2.split(",");
                if (lineArray1[0].equals(lineArray2[1])){
                    out.write(lineArray2[0] + "," + lineArray2[1] + "," + lineArray2[2] + "," + lineArray2[3]+",");
                    in2.close();
                    break;
                }
            }
            //为了BufferedReader每次重新定位到坐标文件的首部
            BufferedReader in3 = new BufferedReader(new FileReader(linkF2));
            //为complex文件加上（目的城市的城市编号，目的城市名称，目的城市经度，目的城市维度）
            while((line2=in3.readLine())!=null){ //两个文件根据城市名进行匹配
                lineArray2 =line2.split(",");
                if (lineArray1[1].equals(lineArray2[1])) {
                    out.write(lineArray2[0] + "," + lineArray2[1] + "," + lineArray2[2] + "," + lineArray2[3]);
                    out.newLine();
                    out.flush();
                    in3.close();
                    break;
                }
            }
        }
        in1.close();
        out.close();
    }
    
    /*
     * 将所有的目的城市的经纬度单独提取出来，用作全国的聚类分析的输入文件
     */
    public void nation() throws IOException{
        //输入文件
        String filePath1="input/complex.csv";
        File linkF = new File(filePath1);
        BufferedReader in = new BufferedReader(new FileReader(linkF));
        //输出文件
        //String filePath2="input/startCity.csv";
        String filePath2="input/endCity.csv";
        File  writeName = new File(filePath2);
        BufferedWriter out = new BufferedWriter(new FileWriter(writeName));
        
        String line,lineArray[];
        while((line=in.readLine())!=null){
            lineArray = line.split(",");
            //out.write(lineArray[2]+","+lineArray[3]);
            out.write(lineArray[6]+","+lineArray[7]);
            out.newLine();
            out.flush();
        }
        in.close();
        out.close();
    }
}
