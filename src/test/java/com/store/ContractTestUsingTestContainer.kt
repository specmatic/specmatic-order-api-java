package com.store

import com.store.model.DB
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ContractTestUsingTestContainer {
    companion object {
        private const val APPLICATION_HOST = "host.docker.internal"
        private const val APPLICATION_PORT = 8090
        private const val EXCLUDED_ENDPOINTS = "'/internal/metrics'"
        private val testContainerEnvVars = mapOf(
            "SPECMATIC_GENERATIVE_TESTS" to "true",
            "SPECMATIC_TEST_PARALLELISM" to "auto",
        )

        private val testContainer: GenericContainer<*> =
            GenericContainer("specmatic/specmatic:latest")
                .withCommand(
                    "test",
                    "--host=$APPLICATION_HOST",
                    "--port=$APPLICATION_PORT",
                    "--filter=PATH!=$EXCLUDED_ENDPOINTS",
                ).withEnv(testContainerEnvVars)
                .withNetworkMode("host")
                .withFileSystemBind(
                    "./specmatic.yaml",
                    "/usr/src/app/specmatic.yaml",
                    BindMode.READ_ONLY,
                ).withFileSystemBind(
                    "./build/reports/specmatic",
                    "/usr/src/app/build/reports/specmatic",
                    BindMode.READ_WRITE,
                ).waitingFor(Wait.forLogMessage(".*Tests run:.*", 1))
                .withLogConsumer { print(it.utf8String) }

        @JvmStatic
        @BeforeAll
        fun setup() {
            DB.resetDB()
        }
    }

    @Test
    fun specmaticContractTest() {
        testContainer.start()
        val hasSucceeded = testContainer.logs.contains("Failures: 0")
        assertThat(hasSucceeded).isTrue()
    }
}
