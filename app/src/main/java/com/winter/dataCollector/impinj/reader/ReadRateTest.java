package com.winter.dataCollector.impinj.reader;

import com.impinj.octane.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReadRateTest {

    public static void main(String[] args) {

        short[] port = new short[]{1};

        /* 根据num设定扫描时长 */
        int duration = 10000;

        double txPowerinDbm = 32.5;


        new Thread(new ReadRateTestFunc("192.168.1.100", port, duration, txPowerinDbm)).start();
//        new Thread(new ReadRateTestFunc("192.168.1.102", port, duration)).start();
    }

}

class ReadRateTestFunc implements Runnable, TagReportListener{

    private final String hostname;
    private final short[] port;
    private final int duration;
    private final double txPowerinDbm;
    private final Map<String, Integer> tm = new HashMap<>();


    public ReadRateTestFunc(String hostname, short[] port, int duration, double txPowerinDbm) {
        this.hostname = hostname;
        this.port = port;
        this.duration = duration;
        this.txPowerinDbm = txPowerinDbm;
    }

    @Override
    public void run() {

        // 读取EPC time
        ImpinjReader reader = new ImpinjReader();
        try {

            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            // Reader 模式
            settings.setReaderMode(ReaderMode.MaxThroughput);
            settings.setTagPopulationEstimate(24);
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
            report.setIncludePeakRssi(true);
            report.setIncludePhaseAngle(true);
            report.setIncludeFirstSeenTime(true);
            report.setIncludeLastSeenTime(true);
            report.setIncludeDopplerFrequency(true);
            report.setIncludeChannel(true);

            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(port);
            for (short portID : port) {
                antennas.getAntenna(portID).setIsMaxRxSensitivity(false);
                antennas.getAntenna(portID).setIsMaxTxPower(false);
                //发射功率
                antennas.getAntenna(portID).setTxPowerinDbm(txPowerinDbm);
                //敏感功率
                antennas.getAntenna(portID).setRxSensitivityinDbm(-80);
            }

            reader.setTagReportListener(this);
            System.out.println("Starting");
            reader.applySettings(settings);

            reader.start();

            Thread.sleep(duration);

            reader.stop();
            reader.disconnect();
            Iterator<Map.Entry<String, Integer>> it = tm.entrySet().iterator();

            Calendar calendar = Calendar.getInstance();

            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                String[] info = entry.getKey().split("_");
                calendar.setTimeInMillis(Long.parseLong(info[1]) * 1000);//转换为毫秒
                Date date = calendar.getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                System.out.println(format.format(date) + " " + hostname + " antenna_" + info[0] + " ReadCount：" + entry.getValue());
            }
        } catch (OctaneSdkException ex) {
            reader.disconnect();
            ex.printStackTrace(System.out);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            reader.disconnect();
            ex.printStackTrace(System.out);
        }
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport tagReport) {
        List<Tag> tags = tagReport.getTags();
        for (Tag t : tags) {
            // 时间的处理
            String epc = t.getEpc().toString();
            long sec = t.getFirstSeenTime().getLocalDateTime().getTime() / 1000;
            String key = t.getAntennaPortNumber() + "_" + sec;
            if (tm.containsKey(key))
                tm.put(key, tm.get(key) + 1);
            else
                tm.put(key, 1);
        }
    }
}
