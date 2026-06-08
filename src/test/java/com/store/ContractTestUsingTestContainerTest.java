package com.store;

import com.store.model.DB;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
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
    private static final int APPLICATION_PORT = 8090;

    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static String enterpriseImage(){
        if(!System.getenv("ENTERPRISE_ARTIFACT_URL").isEmpty()){
            return "specmatic/enterprise:snapshot";
        }else{
            return "specmatic/specmatic:latest";
        }
    }

    private static final GenericContainer<?> testContainer;
    private static final GenericContainer<?> stubContainer;

    static {
        testContainer = new GenericContainer<>(enterpriseImage())
                .withCommand("test")
                .withFileSystemBind("./specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
                .withFileSystemBind("./build/reports/specmatic", "/usr/src/app/build/reports/specmatic", BindMode.READ_WRITE)
                .withNetworkMode("host")
                .waitingFor(Wait.forLogMessage(".*Tests run:.*", 1))
                .withLogConsumer((OutputFrame output) -> System.out.print(output.getUtf8String()));


        stubContainer = new GenericContainer<>(enterpriseImage())
                .withCommand("mock")
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
                .withNetworkMode("host")
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()));
    }

    @BeforeAll
    public static void setup() {
        System.out.println("Running contract tests using Specmatic Test Container against application at localhost:" + APPLICATION_PORT);
        DB.INSTANCE.resetDB();
    }

    @AfterAll
    public static void cleanUp(){
        stubContainer.stop();
        testContainer.stop();
    }

    @Test
    void specmaticContractTest() {
        stubContainer.start();
        testContainer.start();
        boolean hasSucceeded = testContainer.getLogs().contains("Failures: 0");
        Assertions.assertThat(hasSucceeded).isTrue();
    }
}
