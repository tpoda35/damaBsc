package org.dama.damajatek.config;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.exception.AsyncExceptionHandler;
import org.dama.damajatek.exception.ExceptionController;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final ExceptionController exceptionHandler;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setQueueCapacity(150);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler(exceptionHandler);
    }
}
