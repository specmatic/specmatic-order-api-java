package com.store;

import com.store.model.DB;
import io.specmatic.test.SpecmaticContractTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class ContractTest implements SpecmaticContractTest {
    private static ConfigurableApplicationContext context;
    private static final String EXCLUDED_ENDPOINTS = "'/internal/metrics'";
    @BeforeAll
    public static void setUp() {
        System.setProperty("host", "localhost");
        System.setProperty("port", "8090");
        System.setProperty("SPECMATIC_GENERATIVE_TESTS", "true");
        System.setProperty("swaggerUIBaseURL", "http://localhost:8090");
        System.setProperty("SPECMATIC_TEST_PARALLELISM", "auto");
        System.setProperty("filter", String.format("PATH!=%s", EXCLUDED_ENDPOINTS));

        DB.INSTANCE.resetDB();

        context = SpringApplication.run(Application.class);
    }

    @AfterAll
    public static void tearDown() {
        context.close();
    }
}
