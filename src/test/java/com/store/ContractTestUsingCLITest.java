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

    private static final SpecmaticExecutor test = createTestExecutor();
    private static final SpecmaticExecutor stub = createStubExecutor();

    private static SpecmaticExecutor createStubExecutor() {
        List<String> args = asList("virtualize", "--port=" + HTTP_STUB_PORT);
        Map<String, String> env = Map.of("SOME_ENV", "value");
        return new SpecmaticExecutor(args, env);
    }

    private static SpecmaticExecutor createTestExecutor() {
        List<String> args = asList("test", "--host=" + APPLICATION_HOST, "--port=" + APPLICATION_PORT, "--filter=PATH!=" + EXCLUDED_ENDPOINTS);
        Map<String, String> env = Map.of("SPECMATIC_GENERATIVE_TESTS", "true");
        return new SpecmaticExecutor(args, env);
    }

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
        test.verifyNoFailures();
    }

    @AfterAll
    public static void teardown() throws Exception {
        test.stop();
        stub.stop();
    }
}
