package org.dama.damajatek.authentication.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.dto.appUser.AppUserGameStats;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.mapper.AppUserMapper;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.repository.IRoomRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserCacheService implements IAppUserCacheService {

    private final IRoomRepository roomRepository;
    private final IGameRepository gameRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "userProfileCache", key = "#userId")
    @Override
    public AppUserInfoDto loadProfileInfo(Long userId, AppUser loggedInUser) {
        log.info("Caching user profile for user {}.", userId);

        int hostedRooms = roomRepository.countByHostId(userId);
        int joinedRooms = roomRepository.countByOpponentId(userId);

        int vsAiWins = gameRepository.countWinsVsAI(userId);
        int vsAiLoses = gameRepository.countLossesVsAI(userId);

        int vsPlayerWins = gameRepository.countWinsVsPlayer(userId);
        int vsPlayerLoses = gameRepository.countLossesVsPlayer(userId);

        int vsAiDraws = gameRepository.countDrawsVsAI(userId);
        int vsPlayerDraws = gameRepository.countDrawsVsPlayer(userId);

        AppUserGameStats stats = new AppUserGameStats(
                hostedRooms, joinedRooms,
                vsAiWins, vsAiLoses, vsAiDraws,
                vsPlayerWins, vsPlayerLoses, vsPlayerDraws
        );

        return AppUserMapper.createAppUserInfoDto(loggedInUser, stats);
    }

    @CacheEvict(value = "userProfileCache", key = "#userId")
    @Override
    public void evictProfileCache(Long userId) {
        log.info("Evicting user profile cache for user {}.", userId);
    }

    @Override
    public void evictPlayers(Game game) {
        Long redId = game.getRedPlayer().getAppUserId();
        Long blackId = game.getBlackPlayer().getAppUserId();

        var cache = cacheManager.getCache("userProfileCache");
        if (cache != null) {
            if (redId != null) cache.evict(redId);
            if (blackId != null) cache.evict(blackId);
        }
    }

}
