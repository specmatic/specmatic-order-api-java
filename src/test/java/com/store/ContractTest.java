package com.store;

import com.store.model.DB;
import io.specmatic.stub.ContractStub;
import io.specmatic.test.SpecmaticContractTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Optional;

import static io.specmatic.stub.API.createStub;

public class ContractTest implements SpecmaticContractTest {
    private static ConfigurableApplicationContext context;
    private static final String EXCLUDED_ENDPOINTS = "'/internal/metrics'";
    private static ContractStub stub = null;

    @BeforeAll
    public static void setUp() {
        System.setProperty("host", "localhost");
        System.setProperty("port", "8090");
        System.setProperty("SPECMATIC_GENERATIVE_TESTS", "true");
        System.setProperty("SPECMATIC_TEST_PARALLELISM", "auto");
        System.setProperty("filter", String.format("PATH!=%s", EXCLUDED_ENDPOINTS));
        System.setProperty("ORDER_INVENTORY_API_URL", "http://localhost:9000/ws");
        System.out.println("Running contract tests using Specmatic against application at localhost:8080");

        DB.INSTANCE.resetDB();

        stub = createStub("localhost", 9000);

        context = SpringApplication.run(Application.class);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        if(stub != null) {
            stub.close();
        }

        context.close();
    }
}
