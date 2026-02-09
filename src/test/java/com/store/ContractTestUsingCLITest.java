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
    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static final SpecmaticExecutor stub = createStub();
    private static final SpecmaticExecutor test = createTest();

    @BeforeAll
    public static void setup() throws Exception {
        System.out.println("Running contract tests using Specmatic CLI using config from specmatic.yaml");
        DB.INSTANCE.resetDB();
        stub.start();
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
        Map<String, String> env = Map.of();
        return new SpecmaticExecutor(args, env);
    }

    private static SpecmaticExecutor createTest() {
        List<String> args = asList("test");
        Map<String, String> env = Map.of();
        return new SpecmaticExecutor(args, env);
    }
}
