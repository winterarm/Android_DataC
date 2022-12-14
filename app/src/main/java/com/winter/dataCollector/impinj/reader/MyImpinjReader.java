package com.winter.dataCollector.impinj.reader;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.BitPointers;
import com.impinj.octane.ConnectionAttemptEvent;
import com.impinj.octane.ConnectionAttemptListener;
import com.impinj.octane.ConnectionLostListener;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.PcBits;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.SequenceState;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagData;
import com.impinj.octane.TagFilter;
import com.impinj.octane.TagFilterMode;
import com.impinj.octane.TagFilterOp;
import com.impinj.octane.TagOpCompleteListener;
import com.impinj.octane.TagOpReport;
import com.impinj.octane.TagOpResult;
import com.impinj.octane.TagOpSequence;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.TagWriteOp;
import com.impinj.octane.TagWriteOpResult;
import com.impinj.octane.TargetTag;
import com.impinj.octane.WordPointers;
import com.winter.dataCollector.activity.RFIDReaderActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

public class MyImpinjReader implements TagReportListener, ConnectionAttemptListener, ReaderStartListener, ReaderStopListener, ConnectionLostListener, ConnectionEventListener, TagOpCompleteListener {

    private String hostname;
    private String targetMask;
    private short[] port;
    private ReaderMode readerMode;
    private double txPowerinDbm;
    private double rxSensitivityinDbm;
    private boolean masked;
    private boolean isWriteEpcOp;
    private String newEpc;

    private ImpinjReader reader;

    private RFIDReaderActivity activity;

    //TODO 将实验对象编号加入数据当中

    /**
     * 存储要写文件的内容
     * TODO 有时间的话可以把它改成带头尾指针的链表的结构 头指针写，尾指针进行读取，需要注意的是并发读写的问题
     * <p>
     * 实现一个数据存储和处理的类，对接收信号进行监听和处理
     */

    public MyImpinjReader() {
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setTargetMask(String targetMask) {
        this.targetMask = targetMask;
    }

    public void setOnMaskState(boolean state) {
        this.masked = state;
    }

    public void setPowerinDbm(double txPowerinDbm, double rxSensitivityinDbm) {
        this.txPowerinDbm = txPowerinDbm;
        this.rxSensitivityinDbm = rxSensitivityinDbm;
    }

    public void setReaderMode(ReaderMode readerMode) {
        this.readerMode = readerMode;
    }

    public void setPorts(int state) {
        switch (state) {
            case 2:
                port = new short[]{2};
                break;
            case 3:
                port = new short[]{1, 2};
                break;
            case 4:
                port = new short[]{3};
                break;
            case 5:
                port = new short[]{1, 3};
                break;
            case 6:
                port = new short[]{2, 3};
                break;
            case 7:
                port = new short[]{1, 2, 3};
                break;
            case 8:
                port = new short[]{4};
                break;
            case 9:
                port = new short[]{1, 4};
                break;
            case 10:
                port = new short[]{2, 4};
                break;
            case 11:
                port = new short[]{1, 2, 4};
                break;
            case 12:
                port = new short[]{3, 4};
                break;
            case 13:
                port = new short[]{1, 3, 4};
                break;
            case 14:
                port = new short[]{2, 3, 4};
                break;
            case 15:
                port = new short[]{1, 2, 3, 4};
                break;
            default:
                port = new short[]{1};
                break;
        }
    }

    public void startReader(RFIDReaderActivity activity) throws OctaneSdkException {
        this.activity = activity;
        if (null == reader)
            reader = new ImpinjReader();
        initSetting();
        reader.start();
    }

    public void rewriteEpc(RFIDReaderActivity activity, String tarEpc, String nEpc) throws OctaneSdkException {
        this.isWriteEpcOp = true;
        this.newEpc = nEpc.replace(" ", "");
        this.targetMask = tarEpc;
        startReader(activity);
    }

    public boolean testConnected() {
        Log.d(TAG, "Connecting to " + hostname);
        boolean retFlag = false;
        try {
            if (null == reader)
                reader = new ImpinjReader();
            if (reader.isConnected()) {
                reader.stop();
                reader.disconnect();
            }
            reader.connect(hostname);
            retFlag = true;

        } catch (OctaneSdkException e) {
            e.printStackTrace();
        } finally {
            if (reader.isConnected()) {
                reader.disconnect();
            }
        }
        return retFlag;
    }

    private void initSetting() {
        Settings settings = reader.queryDefaultSettings();
        settings.setReaderMode(readerMode);
        if (isWriteEpcOp) {
            settings.setSearchMode(SearchMode.SingleTarget);
            settings.setSession(1);
        }
        if (reader.isConnected()) reader.disconnect();
        try {
            reader.connect(hostname);
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
        // 设置标签读取过滤器settings
        if ((masked || isWriteEpcOp) && StringUtils.isNotEmpty(this.targetMask)) {
            String mask = StringUtils.remove(targetMask, " ");
            TagFilter t1 = settings.getFilters().getTagFilter1();
            t1.setBitCount(mask.length() * 4L);// 掩码位数
            t1.setBitPointer(BitPointers.Epc);
            t1.setMemoryBank(MemoryBank.Epc);
            t1.setFilterOp(TagFilterOp.Match);
            t1.setTagMask(mask);// 80位 12字节
            settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
            settings.getFilters().setTagFilter1(t1);
        }


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
        if(antennas.getAntennaConfigs().size()==0){
            // 天线配置手动初始化
            antennas.getAntennaConfigs().add(new AntennaConfig(1));
            antennas.getAntennaConfigs().add(new AntennaConfig(2));
            antennas.getAntennaConfigs().add(new AntennaConfig(3));
            antennas.getAntennaConfigs().add(new AntennaConfig(4));
        }
        antennas.disableAll();
        try {
            antennas.enableById(port);
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
        try {
            for (short portID : port) {
                antennas.getAntenna(portID).setIsMaxRxSensitivity(false);
                antennas.getAntenna(portID).setIsMaxTxPower(false);
                //发射功率
                antennas.getAntenna(portID).setTxPowerinDbm(this.txPowerinDbm);
                //敏感功率
                antennas.getAntenna(portID).setRxSensitivityinDbm(this.rxSensitivityinDbm);
            }
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
        reader.setTagReportListener(this);
        reader.setConnectionAttemptListener(this);
        reader.setConnectionLostListener(this);
        reader.setReaderStartListener(this);
        reader.setReaderStopListener(this);
        reader.setTagOpCompleteListener(this);
        try {
            reader.applySettings(settings);
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
    }

    public void forceDisConnect() {
        this.isWriteEpcOp = false;
        if (null != reader && reader.isConnected()) {
            try {
                reader.stop();
            } catch (OctaneSdkException e) {
                e.printStackTrace();
            }
            reader.disconnect();
        }
    }

    @Override
    public void onTagReported(ImpinjReader impinjReader, TagReport tagReport) {
//        Log.d(TAG, "====" + System.currentTimeMillis() + "====");
        for (Tag t : tagReport.getTags()) {
            if (isWriteEpcOp) {
                short pc = t.getPcBits();
                String currentEpc = t.getEpc().toHexString();
                try {
                    programEpc(currentEpc, pc, newEpc);
                } catch (Exception e) {
                    Log.d(TAG, "onTagReported: Failed To program EPC: " + e);
                }
            }
        }
        List<Tag> tagsForShow = new ArrayList<>(tagReport.getTags());
        activity.showRFIDData(tagsForShow);// TODO 进程同步的问题
    }


    @Override
    public void onConnectionAttempt(ImpinjReader impinjReader, ConnectionAttemptEvent connectionAttemptEvent) {
        Log.d(TAG, "Trying to connect to reader");
    }

    @Override
    public void onReaderStart(ImpinjReader impinjReader, ReaderStartEvent readerStartEvent) {
        Log.d(TAG, "READER STARTED");
    }

    @Override
    public void onReaderStop(ImpinjReader impinjReader, ReaderStopEvent readerStopEvent) {
        Log.d(TAG, "Reader stopped");
    }

    @Override
    public void onConnectionLost(ImpinjReader impinjReader) {
        Log.d(TAG, "Reader lost");
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
        Log.d(TAG, "Reader closed");
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent event) {
        Log.d(TAG, "connectionErrorOccurred: " + event.toString());
    }

    static short EPC_OP_ID = 123;
    static short PC_BITS_OP_ID = 321;
    static int opSpecID = 1;
    static int outstanding = 0;

    void programEpc(String currentEpc, short currentPC, String newEpc)
            throws Exception {
        if ((currentEpc.length() % 4 != 0) || (newEpc.length() % 4 != 0)) {
            throw new Exception("EPCs must be a multiple of 16- bits: "
                    + currentEpc + "  " + newEpc);
        }

        if (outstanding > 0) {
            return;
        }

        Log.d(TAG, "Programming Tag : EPC " + currentEpc + " to " + newEpc);

        TagOpSequence seq = new TagOpSequence();
        seq.setOps(new ArrayList<>());
        seq.setExecutionCount((short) 1); // delete after one time
        seq.setState(SequenceState.Active);
        seq.setId(opSpecID++);

        seq.setTargetTag(new TargetTag());
        seq.getTargetTag().setBitPointer(BitPointers.Epc);
        seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
        seq.getTargetTag().setData(currentEpc);

        TagWriteOp epcWrite = new TagWriteOp();
        epcWrite.Id = EPC_OP_ID;
        epcWrite.setMemoryBank(MemoryBank.Epc);
        epcWrite.setWordPointer(WordPointers.Epc);
        epcWrite.setData(TagData.fromHexString(newEpc));

        // add to the list
        seq.getOps().add(epcWrite);

        // have to program the PC bits if these are not the same
        if (currentEpc.length() != newEpc.length()) {
            // keep other PC bits the same.
            String currentPCString = PcBits.toHexString(currentPC);

            short newPC = PcBits.AdjustPcBits(currentPC,
                    (short) (newEpc.length() / 4));
            String newPCString = PcBits.toHexString(newPC);

            Log.d(TAG, "PC bits to establish new length: "
                    + newPCString + " " + currentPCString);

            TagWriteOp pcWrite = new TagWriteOp();
            pcWrite.Id = PC_BITS_OP_ID;
            pcWrite.setMemoryBank(MemoryBank.Epc);
            pcWrite.setWordPointer(WordPointers.PcBits);

            pcWrite.setData(TagData.fromHexString(newPCString));
            seq.getOps().add(pcWrite);
        }

        outstanding++;
        reader.addOpSequence(seq);
    }

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {
            System.out.print("  EPC: " + t.getTag().getEpc().toHexString());
            if (t instanceof TagWriteOpResult) {
                TagWriteOpResult tr = (TagWriteOpResult) t;

                if (tr.getOpId() == EPC_OP_ID) {
                    System.out.print("  Write to EPC Complete: ");
                } else if (tr.getOpId() == PC_BITS_OP_ID) {
                    System.out.print("  Write to PC Complete: ");
                }
                System.out.println(" result: " + tr.getResult().toString()
                        + " words_written: " + tr.getNumWordsWritten());
                outstanding--;
            }
        }
    }

    public boolean isConnected() {
        return reader.isConnected();
    }
}