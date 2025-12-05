package com.store;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.store.model.DB;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
public class ContractTestUsingTestContainerTest {
    private static final String dockerHostAddress = System.getProperty("docker.host.address", "host-gateway");
    private static final String APPLICATION_HOST = "host.docker.internal";
    private static final int APPLICATION_PORT = 8090;
    private static final String EXCLUDED_ENDPOINTS = "'/internal/metrics'";
    private static final Integer HTTP_STUB_PORT = 9000;

    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static final GenericContainer<?> testContainer;
    private static final GenericContainer<?> stubContainer;

    static {

        System.out.println("Using docker host address: " + dockerHostAddress);
        testContainer = new GenericContainer<>("specmatic/specmatic:latest")
                .withCommand("test", "--host=" + APPLICATION_HOST, "--port=" + APPLICATION_PORT, "--filter=PATH!=" + EXCLUDED_ENDPOINTS)
//                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("SPECMATIC_GENERATIVE_TESTS", "true")
                .withEnv("SPECMATIC_TEST_PARALLELISM", "auto")
                .withFileSystemBind("./specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
                .withFileSystemBind("./build/reports/specmatic", "/usr/src/app/build/reports/specmatic", BindMode.READ_WRITE)
                // For Docker Desktop on Windows and Mac, "host.docker.internal" is used to refer to the host machine.
                // For Docker on Linux, we use "host-gateway" to refer to the host
                // for tests running in gitlab ci, we set the docker host address via system property
                .withExtraHost("host.docker.internal", dockerHostAddress)
                .waitingFor(Wait.forLogMessage(".*Tests run:.*", 1))
                .withLogConsumer((OutputFrame output) -> System.out.print(output.getUtf8String()));


        stubContainer = new GenericContainer<>("specmatic/specmatic")
//                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withCommand(
                        "virtualize",
                        "--port=" + HTTP_STUB_PORT
                )
                .withCreateContainerCmdModifier(cmd -> {
                    if (cmd.getHostConfig() != null) {
                        cmd.getHostConfig().withPortBindings(
                            new PortBinding(
                                Ports.Binding.bindPort(HTTP_STUB_PORT),
                                new ExposedPort(HTTP_STUB_PORT)
                            )
                        );
                    }
                })
                .withExposedPorts(HTTP_STUB_PORT)
                .withFileSystemBind(
                        "./wsdls",
                        "/usr/src/app/wsdls",
                        BindMode.READ_ONLY
                )
                .withFileSystemBind(
                        "./specmatic.yaml",
                        "/usr/src/app/specmatic.yaml",
                        BindMode.READ_ONLY
                )
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()));
    }

    @BeforeAll
    public static void setup() {
        System.out.println("Running contract tests using Specmatic Test Container against application at " + dockerHostAddress + ":" + APPLICATION_PORT);
        DB.INSTANCE.resetDB();
    }

    @Test
    void specmaticContractTest() {
        System.setProperty("INVENTORY_API_URL", "http://localhost:" + HTTP_STUB_PORT + "/ws");
        stubContainer.start();
        testContainer.start();
        boolean hasSucceeded = testContainer.getLogs().contains("Failures: 0");
        Assertions.assertThat(hasSucceeded).isTrue();
    }
}
