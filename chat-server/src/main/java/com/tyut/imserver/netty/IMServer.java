package com.tyut.imserver.netty;

public interface IMServer {

    boolean isReady();

    void start();

    void stop();
}
