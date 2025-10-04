package org.dama.damajatek.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final ExceptionController exceptionHandler;

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        exceptionHandler.globalExceptionHandler((Exception) ex);
    }
}
