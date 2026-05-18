package com.store;

import com.store.model.DB;
import io.specmatic.stub.ContractStub;
import io.specmatic.test.SpecmaticContractTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import static io.specmatic.stub.API.createStub;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
public class ContractTest implements SpecmaticContractTest {
    public static boolean isNonCIOrLinux() {
        return !"true".equals(System.getenv("CI")) || System.getProperty("os.name").toLowerCase().contains("linux");
    }
    private static ContractStub stub = null;

    @BeforeAll
    public static void setUp() {
        System.out.println("Running contract tests using Specmatic against application at localhost:8090");

        DB.INSTANCE.resetDB();

        stub = createStub("localhost", 9000);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        if(stub != null) {
            stub.close();
        }

    }
}
