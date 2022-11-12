package com.winter.dataCollector.impinj.reader;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 跳频测试
 */
public class ReducedPowerFrequencies implements TagReportListener{

    public static void main(String[] args) {
        ReducedPowerFrequencies obj = new ReducedPowerFrequencies();
        obj.run();
    }

    private void run(){
        try {
            String hostname = "192.168.1.100";

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            reader.setTagReportListener(this);

            FeatureSet features = reader.queryFeatureSet();
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeChannel(true);
            settings.getReport().setMode(ReportMode.Individual);
//            double[] freqAry = new double[]{
//                    920.625, 920.875,
//                    921.125, 921.375, 921.625, 921.875,
//                    922.125, 922.375, 922.625, 922.875,
//                    923.125, 923.375, 923.625, 923.875,
//                    924.125, 924.375
//            };
//
            if (features.isHoppingRegion() && features.getReaderModel() != ReaderModel.SpeedwayR120 && features.getReaderModel() != ReaderModel.SpeedwayR220) {
                // setting reduced power frequencies is allowed if it's hopping
                ArrayList<Double> freqList = new ArrayList<>();
                freqList.add(902.75);
                freqList.add(903.25);
                freqList.add(903.75);
                freqList.add(904.25);
                freqList.add(904.75);
                freqList.add(905.25);
                freqList.add(905.75);
                freqList.add(906.25);
                freqList.add(906.75);
                freqList.add(907.25);
                freqList.add(907.75);
                freqList.add(908.25);
                freqList.add(908.75);
                freqList.add(909.25);
                freqList.add(909.75);
                settings.setTxFrequenciesInMhz(freqList);
            }

            System.out.println("Applying Settings");
            reader.applySettings(settings);

            System.out.println("Starting");
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();

        for (Tag t : tags) {
            System.out.print(" EPC: " + t.getEpc().toString());

            if (reader.getName() != null) {
                System.out.print(" Reader_name: " + reader.getName());
            } else {
                System.out.print(" Reader_ip: " + reader.getAddress());
            }

            if (t.isAntennaPortNumberPresent()) {
                System.out.print(" antenna: " + t.getAntennaPortNumber());
            }

            if (t.isFirstSeenTimePresent()) {
                System.out.print(" first: " + t.getFirstSeenTime().ToString());
            }

            if (t.isLastSeenTimePresent()) {
                System.out.print(" last: " + t.getLastSeenTime().ToString());
            }

            if (t.isSeenCountPresent()) {
                System.out.print(" count: " + t.getTagSeenCount());
            }

            if (t.isRfDopplerFrequencyPresent()) {
                System.out.print(" doppler: " + t.getRfDopplerFrequency());
            }

            if (t.isPeakRssiInDbmPresent()) {
                System.out.print(" peak_rssi: " + t.getPeakRssiInDbm());
            }

            if (t.isChannelInMhzPresent()) {
                System.out.print(" chan_MHz: " + t.getChannelInMhz());
            }

            if (t.isRfPhaseAnglePresent()) {
                System.out.print(" phase angle: " + t.getPhaseAngleInRadians());
            }

            if (t.isFastIdPresent()) {
                System.out.print("\n     fast_id: " + t.getTid().toHexString());

                System.out.print(" model: " +
                        t.getModelDetails().getModelName());

                System.out.print(" epcsize: " +
                        t.getModelDetails().getEpcSizeBits());

                System.out.print(" usermemsize: " +
                        t.getModelDetails().getUserMemorySizeBits());
            }

            System.out.println();
        }
    }
}
