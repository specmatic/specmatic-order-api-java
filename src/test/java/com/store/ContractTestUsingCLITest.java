package com.store;

import com.store.model.DB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
public class ContractTestUsingCLITest {
    private static final String APPLICATION_HOST = "localhost";
    private static final int APPLICATION_PORT = 8090;
    private static final String EXCLUDED_ENDPOINTS = "'/internal/metrics'";
    private static final Integer HTTP_STUB_PORT = 9000;

    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static final SpecmaticExecutor stub = createStub();
    private static final SpecmaticExecutor test = createTest();

    @BeforeAll
    public static void setup() throws Exception {
        System.out.println("Running contract tests using Specmatic CLI against application at " + APPLICATION_HOST + ":" + APPLICATION_PORT);
        DB.INSTANCE.resetDB();
        stub.start();
        System.setProperty("INVENTORY_API_URL", "http://localhost:" + HTTP_STUB_PORT + "/ws");
    }

    @Test
    void specmaticContractTest() throws Exception {
        test.start();
        test.verifySuccessfulExecutionWithNoFailures();
    }

    @AfterAll
    public static void teardown() throws Exception {
        test.stop();
        stub.stop();
    }

    private static SpecmaticExecutor createStub() {
        List<String> args = asList("virtualize");
        return new SpecmaticExecutor(args, emptyMap());
    }

    private static SpecmaticExecutor createTest() {
        List<String> args = asList("test");
        return new SpecmaticExecutor(args, emptyMap());
    }
}
