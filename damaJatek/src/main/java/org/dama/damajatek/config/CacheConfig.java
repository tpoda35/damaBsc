package org.dama.damajatek.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of(
                "userProfileCache",
                "botCache"
        ));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return newBuilder()
                .maximumSize(600)
                .expireAfterAccess(30, MINUTES)
                .recordStats();
    }
}