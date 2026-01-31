package org.dama.damajatek.dto.appUser;

public record AppUserGameStats(
        int hostedRooms,
        int joinedRooms,
        int vsAiWins,
        int vsAiLoses,
        int vsAiDraws,
        int vsPlayerWins,
        int vsPlayerLoses,
        int vsPlayerDraws
) {

    // Total games vs ai
    public int vsAiGames() {
        return vsAiWins + vsAiLoses + vsAiDraws;
    }

    public int vsPlayerGames() {
        return vsPlayerWins + vsPlayerLoses + vsPlayerDraws;
    }

    public int overallGames() {
        return vsAiGames() + vsPlayerGames();
    }

    // Winrate with draws counted as half-win
    public int vsBotWinrate() {
        int total = vsAiGames();
        if (total == 0) return 0;

        double rate = (vsAiWins + 0.5 * vsAiDraws) * 100.0 / total;
        return (int) Math.round(rate);
    }

    public int vsPlayerWinrate() {
        int total = vsPlayerGames();
        if (total == 0) return 0;

        double rate = (vsPlayerWins + 0.5 * vsPlayerDraws) * 100.0 / total;
        return (int) Math.round(rate);
    }

    public int overallWinrate() {
        int total = overallGames();
        if (total == 0) return 0;

        double rate = (vsAiWins + 0.5 * vsAiDraws + vsPlayerWins + 0.5 * vsPlayerDraws) * 100.0 / total;
        return (int) Math.round(rate);
    }
}
