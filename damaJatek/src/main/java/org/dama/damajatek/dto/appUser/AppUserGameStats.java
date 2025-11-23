package org.dama.damajatek.dto.appUser;

public record AppUserGameStats(
        int hostedRooms,
        int joinedRooms,
        int vsAiWins,
        int vsAiLoses,
        int vsPlayerWins,
        int vsPlayerLoses
) {
    public int vsAiGames() {
        return vsAiWins + vsAiLoses;
    }

    public int vsPlayerGames() {
        return vsPlayerWins + vsPlayerLoses;
    }
    
    public int overallGames() {
        return vsAiGames() + vsPlayerGames();
    }

    public int vsBotWinrate() {
        return vsAiGames() == 0 ? 0 : (vsAiWins * 100) / vsAiGames();
    }

    public int vsPlayerWinrate() {
        return vsPlayerGames() == 0 ? 0 : (vsPlayerWins * 100) / vsPlayerGames();
    }

    public int overallWinrate() {
        return overallGames() == 0
                ? 0
                : ((vsAiWins + vsPlayerWins) * 100) / overallGames();
    }
}
