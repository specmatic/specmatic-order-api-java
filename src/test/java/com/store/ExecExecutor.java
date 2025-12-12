package com.store;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

class ExecExecutor implements Executor {
    private final ManagedProcess process;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);

    ExecExecutor(ManagedProcessBuilder builder) {
        this.process = builder.setDestroyOnShutdown(true).setConsoleBufferMaxLines(0)
                // write to both console and capture in memory
                .addStdOut(System.out).addStdOut(outputStream).addStdErr(System.err).addStdErr(outputStream).build();

    }

    @Override
    public void start() throws Exception {
        process.start();
    }

    @Override
    public void stop() throws Exception {
        process.waitForExitMaxMsOrDestroy(10000);

    }

    @Override
    public String getLogs() {
        if (!process.isAlive()) {
            throw new IllegalStateException("Process has not been started yet.");
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public int exitCode() throws Exception {
        if (process.isAlive()) {
            throw new IllegalStateException("Process has not been started or completed yet.");
        }

        return process.exitValue();
    }

    @Override
    public void waitForExit() throws Exception {
        process.waitForExit();
    }
}
