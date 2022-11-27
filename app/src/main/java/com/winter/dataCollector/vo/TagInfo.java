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
    public Double avg_rssi = (double) 0;
    public double max_rssi = 0;
    public int del_cnt = 5;
    List<Double> rssis = new ArrayList<>();
    List<Double> phases = new ArrayList<>();

    public TagInfo(String epc) {
        Epc = epc;
    }

    public void addInfo(double rssi, double phase) {
        rssis.add(rssi);
        phases.add(phase);
        this.readiedNum++;
    }

    public boolean updateInfo() {
        if (readiedNum > 0 && lastNum != readiedNum) {
            // 该标签存在更新的信息
            DoubleSummaryStatistics summaryStatistics = rssis.stream().mapToDouble(Number::doubleValue).summaryStatistics();
            avg_rssi = BigDecimal.valueOf(summaryStatistics.getAverage()).setScale(2, RoundingMode.UP).doubleValue();
            max_rssi = summaryStatistics.getMax();
            lastNum = readiedNum;
            del_cnt++;
        } else if (del_cnt > 0) {
            del_cnt--;
        }
        return del_cnt > 0;
    }
}
