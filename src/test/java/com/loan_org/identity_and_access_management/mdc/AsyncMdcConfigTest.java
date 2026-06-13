package com.loan_org.identity_and_access_management.mdc;

import com.loan_org.identity_and_access_management.middleware.config.AsyncMdcConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncMdcConfigTest {

    private AsyncMdcConfig config;

    @BeforeEach
    void setUp() {
        config = new AsyncMdcConfig();
        // Manually inject values normally supplied by @Value
        ReflectionTestUtils.setField(config, "corePoolSize", 2);
        ReflectionTestUtils.setField(config, "maxPoolSize", 4);
        ReflectionTestUtils.setField(config, "queueCapacity", 10);
        ReflectionTestUtils.setField(config, "threadNamePrefix", "test-async-");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_ConfigureThreadPoolTaskExecutorCorrectly() {
        // When
        Executor executor = config.taskExecutor();

        // Then
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor poolExecutor = (ThreadPoolTaskExecutor) executor;

        assertThat(poolExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(poolExecutor.getMaxPoolSize()).isEqualTo(4);
        assertThat(poolExecutor.getThreadNamePrefix()).isEqualTo("test-async-");

        // Clean up the created thread pool
        poolExecutor.shutdown();
    }

    @Test
    void should_PropagateMdc_When_ContextMapIsPresent() throws InterruptedException {
        // Given: Build the executor and extract the package-private decorator safely using reflection
        ThreadPoolTaskExecutor poolExecutor = (ThreadPoolTaskExecutor) config.taskExecutor();
        TaskDecorator decorator = (TaskDecorator) ReflectionTestUtils.getField(poolExecutor, "taskDecorator");

        assertThat(decorator).isNotNull();
        MDC.put("traceId", "success-trace-123");

        AtomicReference<String> capturedTraceIdInWorker = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Decorate a dummy runnable task
        Runnable baseRunnable = () -> {
            capturedTraceIdInWorker.set(MDC.get("traceId"));
            latch.countDown();
        };
        Runnable decoratedRunnable = decorator.decorate(baseRunnable);

        // When: Run it on a background thread to simulate the worker execution
        Thread backgroundThread = new Thread(decoratedRunnable);
        backgroundThread.start();

        boolean completed = latch.await(2, TimeUnit.SECONDS);

        // Then
        assertThat(completed).isTrue();
        assertThat(capturedTraceIdInWorker.get()).isEqualTo("success-trace-123");

        poolExecutor.shutdown();
    }

    @Test
    void should_HandleGracefully_When_ContextMapIsNull() throws InterruptedException {
        // Given: Force an empty/null MDC context map
        MDC.clear();

        ThreadPoolTaskExecutor poolExecutor = (ThreadPoolTaskExecutor) config.taskExecutor();
        TaskDecorator decorator = (TaskDecorator) ReflectionTestUtils.getField(poolExecutor, "taskDecorator");

        assertThat(decorator).isNotNull();
        AtomicReference<Boolean> wasExecuted = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable baseRunnable = () -> {
            assertThat(MDC.getCopyOfContextMap()).isNull();
            wasExecuted.set(true);
            latch.countDown();
        };
        Runnable decoratedRunnable = decorator.decorate(baseRunnable);

        // When
        Thread backgroundThread = new Thread(decoratedRunnable);
        backgroundThread.start();
        boolean completed = latch.await(2, TimeUnit.SECONDS);

        // Then
        assertThat(completed).isTrue();
        assertThat(wasExecuted.get()).isTrue();

        poolExecutor.shutdown();
    }
}