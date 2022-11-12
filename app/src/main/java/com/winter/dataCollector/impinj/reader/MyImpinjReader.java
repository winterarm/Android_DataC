package com.winter.dataCollector.impinj.reader;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.ConnectionAttemptEvent;
import com.impinj.octane.ConnectionAttemptListener;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.winter.dataCollector.impinj.utils.FileWriteUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyImpinjReader implements TagReportListener, ConnectionAttemptListener, ReaderStartListener, ReaderStopListener {

    private final String hostname;
    private final short[] port;

    private com.impinj.octane.ImpinjReader reader;


    //TODO 将实验对象编号加入数据当中

    /**
     * 存储要写文件的内容
     * TODO 有时间的话可以把它改成带头尾指针的链表的结构 头指针写，尾指针进行读取，需要注意的是并发读写的问题
     *
     * 实现一个数据存储和处理的类，对接收信号进行监听和处理
     */
    private Map<String, ArrayList<String>> report_infos;

    public MyImpinjReader(String hostname, short[] port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void startReader() {
        // 读取EPC time
        if(null == reader)
            reader = new com.impinj.octane.ImpinjReader();
        report_infos = new HashMap<>();
        Log.d(TAG, "Connecting to " + hostname);
        try {
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
//            settings.setReaderMode(ReaderMode.AutoSetDenseReader);
            settings.setReaderMode(ReaderMode.MaxThroughput);

            // 设置标签读取过滤器settings
//            TagFilter t1 = settings.getFilters().getTagFilter1();
//            t1.setBitCount(targetmask.length() * 4);// 掩码位数
//            t1.setBitPointer(BitPointers.Epc);
//            t1.setMemoryBank(MemoryBank.Epc);
//            t1.setFilterOp(TagFilterOp.Match);
//            t1.setTagMask(targetmask);// 80位 12字节
//
//            settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
//            settings.getFilters().setTagFilter1(t1);

            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setIncludeFastId(true);
            report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
            report.setIncludePeakRssi(true);
            report.setIncludeDopplerFrequency(true);
            report.setIncludePhaseAngle(true);
            report.setIncludeFirstSeenTime(true);
            report.setIncludeLastSeenTime(true);
            report.setIncludeChannel(true);

            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(port);
            for (short portID : port) {
                antennas.getAntenna(portID).setIsMaxRxSensitivity(false);
                antennas.getAntenna(portID).setIsMaxTxPower(false);
                //发射功率
                antennas.getAntenna(portID).setTxPowerinDbm(32.5);
                //敏感功率
                antennas.getAntenna(portID).setRxSensitivityinDbm(-70);
            }
//        long startmus = (System.currentTimeMillis() + 48870) * 1000;
            reader.setTagReportListener(this);
            reader.setConnectionAttemptListener(this);
            reader.setReaderStartListener(this);
            reader.setReaderStopListener(this);
            reader.applySettings(settings);
            reader.start();

            Log.d(TAG, "READER STARTED");
//            保持测量一段时间或等待控制台输入

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopReaderAndSaveData(String filepath) throws OctaneSdkException {
        reader.stop();
        reader.disconnect();
        Log.d(TAG, "READER STOPPED AND DISCONNECTED");

        // 文件读写 一次性读写 可以优化
        int dirIdx = 1;
        File dir = new File(filepath + "/" + dirIdx);
        while (dir.exists()) {
            dirIdx++;
            dir = new File(filepath + "/" + dirIdx);
        }
        for (Map.Entry<String, ArrayList<String>> entry : report_infos.entrySet()) {
            // 写文件
            String key = entry.getKey();
            String[] keys = key.split("_");
            String fileDir = filepath + "/" + dirIdx + (keys.length > 1 && port.length > 1 ? "/" + keys[1] : "");
            FileWriteUtil.writefileM(fileDir, keys[0].substring(keys[0].length() - 4), entry.getValue());
        }
    }

    @Override
    public void onTagReported(com.impinj.octane.ImpinjReader impinjReader, TagReport tagReport) {
        List<Tag> tags = tagReport.getTags();
        Log.d(TAG, "====" + System.currentTimeMillis() + "====");
        for (Tag t : tags) {
            // 保存上报的标签数据到内存中
            saveTagInfo(t);
        }
    }

    private void saveTagInfo(Tag t) {
        String epc = t.getEpc().toString();
        long time = Long.parseLong(t.getFirstSeenTime().ToString());
        String val = time
                + "," + t.getAntennaPortNumber()
                + "," + t.getPhaseAngleInRadians()
                + "," + t.getPeakRssiInDbm()
                + "," + t.getRfDopplerFrequency()
                ;
//        根据标签EPC存储反射信号信息
        if (report_infos.containsKey(epc)) {
            Objects.requireNonNull(report_infos.get(epc)).add(val);
        } else {
            report_infos.put(epc, new ArrayList<String>() {{
                add(val);
            }});
        }
    }

    @Override
    public void onConnectionAttempt(com.impinj.octane.ImpinjReader impinjReader, ConnectionAttemptEvent connectionAttemptEvent) {
        Log.d(TAG, "Trying to connect to reader");
    }

    @Override
    public void onReaderStart(com.impinj.octane.ImpinjReader impinjReader, ReaderStartEvent readerStartEvent) {
        Log.d(TAG, "Reader started");
    }

    @Override
    public void onReaderStop(com.impinj.octane.ImpinjReader impinjReader, ReaderStopEvent readerStopEvent) {
        Log.d(TAG, "Reader stopped");
    }

}