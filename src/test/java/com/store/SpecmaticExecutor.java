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
    private final String command;
    private Process process;
    private Thread stdOut;
    private Thread stdErr;

    private final StringBuffer logs = new StringBuffer(8192);

    SpecmaticExecutor(List<String> args, Map<String, String> env) {
        if (args.isEmpty())
            throw new IllegalArgumentException("At least one argument is required to execute Specmatic");
        this.command = "Specmatic " + args.get(0);
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
        System.out.println("Starting " + command);
        process = builder.start();
        this.stdOut = startStreamThread(process.getInputStream(), System.out, command + ":STDOUT");
        this.stdErr = startStreamThread(process.getErrorStream(), System.err, command + ":STDERR");
    }

    public void stop() throws Exception {
        System.out.println("Stopping " + command);
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

    public void verifySuccessfulExecutionWithNoFailures() throws Exception {
        System.out.println("Verifying " + command + " completed without failures");
        if (process == null) throw new IllegalStateException(command + " process has not been started");
        process.waitFor();
        int exitCode = process.exitValue();
        assertThat(exitCode)
                .withFailMessage("Expected %s to exit without any failures, but it exited with code %%d".formatted(command), exitCode)
                .isEqualTo(0);
        boolean hasSucceeded = logs.toString().contains("Failures: 0");
        assertThat(hasSucceeded)
                .withFailMessage("Expected " + command + " to report 0 failures but some tests have failed")
                .isTrue();
    }

    private Thread startStreamThread(InputStream in, java.io.PrintStream out, String label) {
        Thread t = new Thread(() -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String entry = "[%s] %s%s".formatted(label, line, System.lineSeparator());
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
}
