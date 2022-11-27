package com.winter.dataCollector.impinj;

import com.impinj.octane.ReaderMode;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    private static Constants Constants;

    class Pair {
        private String key;
        private double val;

        public Pair(String key, double val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public static Pair[] txPowerInDbm = null;
    public static Pair[] rxSensitiveDbm = null;

    {
        List<Pair> temp = new ArrayList<>();
        for (double i = 32.5; i >= 10; i-=0.5) {
            temp.add(new Pair(""+i, i));
        }
        txPowerInDbm = temp.toArray(new Pair[0]);

        temp.clear();
        for (double i = -80; i < -30; i+=5) {
            temp.add(new Pair(""+i, i));
        }
        rxSensitiveDbm = temp.toArray(new Pair[0]);
    }



    private final MyReaderMode[] readerModes = new MyReaderMode[]{
            new MyReaderMode(ReaderMode.MaxThroughput),
            new MyReaderMode(ReaderMode.Hybrid),
            new MyReaderMode(ReaderMode.DenseReaderM4),
            new MyReaderMode(ReaderMode.DenseReaderM8),
            new MyReaderMode(ReaderMode.MaxMiller),
            new MyReaderMode(ReaderMode.DenseReaderM4Two),
            new MyReaderMode(ReaderMode.AutoSetDenseReader),
            new MyReaderMode(ReaderMode.AutoSetDenseReaderDeepScan),
            new MyReaderMode(ReaderMode.AutoSetStaticFast),
            new MyReaderMode(ReaderMode.AutoSetStaticDRM)
    };

    public static MyReaderMode[] getModes(){
        if(Constants == null){
            Constants = new Constants();
        }
        return Constants.readerModes;
    }


}
