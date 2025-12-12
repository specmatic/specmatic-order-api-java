package com.store;

import org.testcontainers.containers.GenericContainer;

class TestContainerExecutor<T extends GenericContainer<T>> implements Executor {
    private final GenericContainer<T> container;

    TestContainerExecutor(GenericContainer<T> container) {
        this.container = container;
    }

    @Override
    public void start() throws Exception {
        container.start();
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public String getLogs() {
        return container.getLogs();
    }

    @Override
    public int exitCode() {
        Long exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong();
        if (exitCode == null) {
            throw new IllegalStateException("No exit code available yet. Container is still in " + container.getCurrentContainerInfo().getState().getStatus() + " state.");
        }
        return exitCode.intValue();
    }

    @Override
    public void waitForExit() {
        // do nothing
    }
}
