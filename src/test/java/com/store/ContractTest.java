package com.store;

import com.store.model.DB;
import io.specmatic.stub.ContractStub;
import io.specmatic.test.SpecmaticContractTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static io.specmatic.stub.API.createStub;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContractTest implements SpecmaticContractTest {
    private static ContractStub stub = null;
    @BeforeAll
    public static void setup() throws Exception {
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
