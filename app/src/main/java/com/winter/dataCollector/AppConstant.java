package com.winter.dataCollector;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppConstant {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("MMdd", Locale.CHINA);

    /**
     * 一些测试代码
     * @param args
     */
    public static void main(String[] args) {
        int p = 0;
        p=p^1;
        System.out.println(p);
        p=p^8;
        System.out.println(p);
        p=p^4;
        System.out.println(p);
        p=p^8;
        System.out.println(p);

    }
}
