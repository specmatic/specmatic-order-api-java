package com.store;

interface Executor {
    void start() throws Exception;

    void stop() throws Exception;

    String getLogs();

    int exitCode() throws Exception;

    void waitForExit() throws Exception;
}
