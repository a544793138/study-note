package com.union.api;

import com.union.utils.Checker;
import com.union.utils.Tester;

public class TestAPI {

    private static UnionCSSP cssp = new UnionCSSP();

    private static String keyName = "pboc.103102.mk-ac";
    private static String version = "0";
    private static String pan = "6354179812717000";
    private static String atc = "00FF";
    private static String arqcData = "000000000000000000000000084080000100000840010101019ACEE110000000FF03A0B000";
    private static String arqc = "00FD1992A6F30D31";
    private static String iccType = "1";

    public static void testE301() {
        UnionCSSP.Recv recv = cssp.servE301(keyName, version, pan, atc, arqcData, arqc, iccType);
        Checker.checkState(recv.getResponseCode() == 0);
    }

    public static void main(String[] args) {
        Tester.Configurer configurer = new Tester.Configurer().withThreads(20).withDurationSeconds(15 * 60).addRTTInterception(5);
        Tester.execute(configurer, new Runnable() {
            @Override
            public void run() {
                testE301();
            }
        });
    }

}
