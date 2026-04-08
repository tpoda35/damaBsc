package org.dama.damajatek.service;

public interface IGameScheduler {
    void scheduleTimeout(String email, Long gameId);
    void cancelSchedule(String email);
}
