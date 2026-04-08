package org.dama.damajatek.service;

import org.springframework.security.core.Authentication;

public interface IGameScheduler {
    void scheduleTimeout(String email, Long gameId, Authentication auth);
    void cancelSchedule(String email);
}
