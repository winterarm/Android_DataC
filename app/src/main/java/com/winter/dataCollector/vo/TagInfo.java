package com.winter.dataCollector.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class TagInfo {
    public String Epc;
    public int lastNum = 0;
    public int readiedNum = 0;
    public Double rssi_last = (double) 0;
    public Double rssi_avg = (double) 0;
    public double rssi_max = 0;
    public int del_cnt = 5;
    List<Double> rssis = new ArrayList<>();
    List<Double> phases = new ArrayList<>();
    List<Double> rssis_last = new ArrayList<>();

    public TagInfo(String epc) {
        Epc = epc;
    }

    public void addInfo(double rssi, double phase) {
        rssi_last = rssi;
        rssis.add(rssi);
        rssis_last.add(rssi);
        phases.add(phase);
        this.readiedNum++;
        if(readiedNum > 50)
            rssis_last.remove(0);

    }

    public boolean updateInfo() {
        if (readiedNum > 0 && lastNum != readiedNum) {
            // 该标签存在更新的信息
            DoubleSummaryStatistics summaryStatistics = rssis_last.stream().mapToDouble(Number::doubleValue).summaryStatistics();
            rssi_avg = BigDecimal.valueOf(summaryStatistics.getAverage()).setScale(2, RoundingMode.UP).doubleValue();
            rssi_max = summaryStatistics.getMax();
            lastNum = readiedNum;
            del_cnt++;
        } else if (del_cnt > 0) {
            del_cnt--;
        }
        return del_cnt > 0;
    }
}
