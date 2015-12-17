package com.finalproject.mosapp;

/**
 * Created by ythogh on 12/15/2015.
 */
public class MosaicBuilderOptions {

    public boolean doBlend;

    public MosaicBuilderOptions() {
        this(true);
    }

    public MosaicBuilderOptions(boolean doBlend) {
        this.doBlend = doBlend;
    }

    public void setDoBlend(boolean doBlend) {
        this.doBlend = doBlend;
    }

}
