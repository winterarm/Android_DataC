package com.winter.dataCollector.impinj;

import com.impinj.octane.ReaderMode;

public class MyReaderMode {
    private final ReaderMode key;

    public MyReaderMode(ReaderMode key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key.name();
    }

    public ReaderMode getReaderMode(){
        return this.key;
    }
}
