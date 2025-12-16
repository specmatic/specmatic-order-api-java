package com.store;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class SpecmaticExecutor {
    private final ProcessBuilder builder;
    private Process process;
    private Thread stdOut;
    private Thread stdErr;

    private final StringBuffer logs = new StringBuffer(8192);

    SpecmaticExecutor(List<String> args, Map<String, String> env) {
        try {
            List<String> cmd = new ArrayList<>(asList("java", "-jar", System.getProperty("user.home") + "/.specmatic/specmatic.jar"));
            cmd.addAll(args);
            builder = new ProcessBuilder(cmd);
            builder.environment().putAll(env);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() throws Exception {
        process = builder.start();
        this.stdOut = startStreamThread(process.getInputStream(), System.out, "STDOUT");
        this.stdErr = startStreamThread(process.getErrorStream(), System.err, "STDERR");
    }

    private Thread startStreamThread(InputStream in, java.io.PrintStream out, String label) {
        Thread t = new Thread(() -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String entry = "[" + label + "] " + line + System.lineSeparator();
                    logs.append(entry);
                    out.println(line);
                }
            } catch (Exception ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    public void stop() throws Exception {
        if (process == null) return;

        // wait up to 10s, then forcibly destroy
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            try {
                process.destroy();
                process.waitFor(1, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }

        // ensure reader threads finish
        if (stdOut != null) stdOut.join(1000);
        if (stdErr != null) stdErr.join(1000);
    }

    public String getLogs() {
        if (process == null) {
            throw new IllegalStateException("Specmatic process has not been started yet.");
        }
        return logs.toString();
    }

    public int exitCode() {
        if (process == null || process.isAlive()) {
            throw new IllegalStateException("Specmatic process has not been started or completed yet.");
        }

        return process.exitValue();
    }

    public void verifyNoFailures() throws Exception {
        if (process != null) process.waitFor();
        assertThat(exitCode())
                .withFailMessage("Expected Specmatic to exit without any failures, but it exited with code %d", exitCode())
                .isEqualTo(0);
        boolean hasSucceeded = getLogs().contains("Failures: 0");
        assertThat(hasSucceeded)
                .withFailMessage("Expected Specmatic to report 0 failures but some tests have failed")
                .isTrue();
    }
}
