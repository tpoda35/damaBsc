package org.dama.damajatek.authentication.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.dto.appUser.AppUserGameStats;
import org.dama.damajatek.mapper.AppUserMapper;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.repository.IRoomRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserCacheService implements IAppUserCacheService {

    private final IRoomRepository roomRepository;
    private final IGameRepository gameRepository;

    @Cacheable(value = "userCache", key = "#userId")
    @Override
    public AppUserInfoDto loadProfileInfo(Long userId, AppUser loggedInUser) {
        log.info(">>> Loading user info (not cached!)");

        int hostedRooms = roomRepository.countByHostId(userId);
        int joinedRooms = roomRepository.countByOpponentId(userId);

        int vsAiWins = gameRepository.countWinsVsAI(userId);
        int vsAiLoses = gameRepository.countLossesVsAI(userId);

        int vsPlayerWins = gameRepository.countWinsVsPlayer(userId);
        int vsPlayerLoses = gameRepository.countLossesVsPlayer(userId);

        AppUserGameStats stats = new AppUserGameStats(
                hostedRooms, joinedRooms,
                vsAiWins, vsAiLoses,
                vsPlayerWins, vsPlayerLoses
        );

        return AppUserMapper.createAppUserInfoDto(loggedInUser, stats);
    }
}
