package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.service.IGameScheduler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class GameScheduler implements IGameScheduler {

    private final TaskScheduler taskScheduler;
    private final GameService gameService;

    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    @Override
    public void scheduleTimeout(String email, Long gameId, Authentication auth) {
        // Interrupt the old one
        ScheduledFuture<?> old = tasks.remove(email);
        if (old != null) old.cancel(false);

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> gameService.handleTimeout(email, auth),
                Instant.now().plusSeconds(30)
        );

        tasks.put(email, future);
    }

    @Override
    public void cancelSchedule(String email) {
        ScheduledFuture<?> future = tasks.remove(email);
        if (future != null) future.cancel(false);
    }
}
