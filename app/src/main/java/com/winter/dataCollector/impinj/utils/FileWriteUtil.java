package com.winter.dataCollector.impinj.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileWriteUtil {


    /**
     * 写单个的文件
     *
     * @param filename 传入的文件名
     * @param content  每一行的内容
     * @param <T>      字符串或数字
     */
    public static <T> void writefileS(String filename, ArrayList<T> content) {
        File file = new File(filename + ".csv");

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (T t : content) {
                String temp = (String) t;
                bw.write(temp); // 写入所有的EPC,RSSI,Phase,Hz,time,天线号
                // bw.write("," + (i + 1));// ,id
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 连续写多个文件
     *
     * @param filename 传入的文件名
     * @param content  每一行的内容
     * @param <T>      字符或数字
     */
    public static <T> void writefileM(String filename, ArrayList<T> content) {
        String fileFolderName = ".";
        int fileCount = 0;
        int step = 1;

        File fileFind = new File(fileFolderName);
        File[] fileArray = fileFind.listFiles();
        assert fileArray != null;
        for (File value : fileArray) {
            if (value.isFile() && value.getName().startsWith(filename)) {
                fileCount += step;
            }
        }
        File file = new File(fileFolderName + "/" + filename + "_" + fileCount + ".csv");

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (T t : content) {
                String temp = t.toString();
                bw.write(temp); // 写入所有的EPC,RSSI,Phase,Hz,time,天线号
                // bw.write("," + (i + 1));// ,id
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 连续写多个文件
     *
     * @param filename 传入的文件名
     * @param content  每一行的内容
     * @param <T>      字符或数字
     */
    public static <T> void writefileM(String fileFolderName, String filename, ArrayList<T> content) {
        File fileFind = new File(fileFolderName);
        if (!fileFind.exists())
            fileFind.mkdirs();
        File[] fileArray = fileFind.listFiles();
        assert fileArray != null;
        filename = fileFolderName + "/" + filename + ".csv";
        File file = new File(filename);

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (T t : content) {
                String temp = t.toString();
                bw.write(temp); // 写入所有的EPC,RSSI,Phase,Hz,time,天线号
                // bw.write("," + (i + 1));// ,id
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("请于当前目录下查看保存的" + filename + "数据文件");
    }
}
