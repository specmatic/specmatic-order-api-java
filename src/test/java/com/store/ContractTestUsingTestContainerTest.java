package com.store;

import ch.vorburger.exec.ManagedProcessBuilder;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.store.model.DB;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
public class ContractTestUsingTestContainerTest {
    private static final String APPLICATION_HOST = runWithTestContainers() ? System.getProperty("docker.host.address", "host-gateway") : "localhost";
    private static final int APPLICATION_PORT = 8090;
    private static final String EXCLUDED_ENDPOINTS = "'/internal/metrics'";
    private static final Integer HTTP_STUB_PORT = 9000;

    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static final Executor testExecutor;
    private static final Executor stubExecutor;

    static {
        testExecutor = createTestExecutor();
        stubExecutor = createStubExecutor();
    }

    private static Executor createStubExecutor() {
        String[] args = {"virtualize", "--port=" + HTTP_STUB_PORT};
        Map<String, String> env = new HashMap<>() {
            {
                put("SOME_ENV", "value");
            }
        };


        if (runWithTestContainers()) {
            return new TestContainerExecutor(createStubContainer(args, env));
        } else {
            return new ExecExecutor(createSpecmaticProcess(args, env));
        }
    }

    private static boolean runWithTestContainers() {
        return false;
    }

    private static Executor createTestExecutor() {
        String[] args = {"test", "--host=" + APPLICATION_HOST, "--port=" + APPLICATION_PORT, "--filter=PATH!=" + EXCLUDED_ENDPOINTS};

        Map<String, String> env = new HashMap<>() {
            {
                put("SPECMATIC_GENERATIVE_TESTS", "true");
                put("SPECMATIC_TEST_PARALLELISM", "auto");
            }
        };


        if (runWithTestContainers()) {
            return new TestContainerExecutor(createTestContainer(args, env));
        } else {
            return new ExecExecutor(createSpecmaticProcess(args, env));
        }
    }

    private static ManagedProcessBuilder createSpecmaticProcess(String[] args, Map<String, String> env) {
        try {
            ManagedProcessBuilder specmatic = new ManagedProcessBuilder("java").addArgument("-jar").addArgument("/Users/ketan/.specmatic/specmatic.jar");
            specmatic.getEnvironment().putAll(env);
            for (String arg : args) {
                specmatic.addArgument(arg, false);
            }
            return specmatic;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static GenericContainer<?> createStubContainer(String[] args, Map<String, String> env) {
        return new GenericContainer<>("specmatic/specmatic:latest").withCommand(args).withEnv(env).withCreateContainerCmdModifier(cmd -> {
            cmd.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(HTTP_STUB_PORT), new ExposedPort(HTTP_STUB_PORT)));
        }).withExposedPorts(HTTP_STUB_PORT).withFileSystemBind(".", "/usr/src/app").waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200)).withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()));
    }

    private static GenericContainer<?> createTestContainer(String[] args, Map<String, String> env) {
        GenericContainer<?> container = new GenericContainer<>("specmatic/specmatic:latest").withCommand(args).withEnv(env).withFileSystemBind(".", "/usr/src/app")
                // For Docker Desktop on Windows and Mac, "host.docker.internal" is used to refer to the host machine.
                // For Docker on Linux, we use "host-gateway" to refer to the host
                // for tests running in gitlab ci, we set the docker host address via system property
                .withExtraHost("host.docker.internal", APPLICATION_HOST).waitingFor(Wait.forLogMessage(".*Tests run:.*", 1)).withLogConsumer((OutputFrame output) -> System.out.print(output.getUtf8String()));
        return container;
    }

    @BeforeAll
    public static void setup() {
        System.out.println("Running contract tests using Specmatic Test Container against application at " + APPLICATION_HOST + ":" + APPLICATION_PORT);
        DB.INSTANCE.resetDB();
    }

    @AfterAll
    public static void teardown() throws Exception {
        testExecutor.stop();
        stubExecutor.stop();
    }

    @Test
    void specmaticContractTest() throws Exception {
        System.setProperty("INVENTORY_API_URL", "http://localhost:" + HTTP_STUB_PORT + "/ws");
        stubExecutor.start();
        testExecutor.start();
        testExecutor.waitForExit();
        boolean hasSucceeded = testExecutor.getLogs().contains("Failures: 0");
        Assertions.assertThat(hasSucceeded).isTrue();
    }
}
