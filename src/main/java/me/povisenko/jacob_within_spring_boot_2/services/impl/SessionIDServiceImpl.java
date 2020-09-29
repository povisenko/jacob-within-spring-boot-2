package me.povisenko.jacob_within_spring_boot_2.services.impl;

import lombok.RequiredArgsConstructor;
import me.povisenko.jacob_within_spring_boot_2.services.DllApiService;
import me.povisenko.jacob_within_spring_boot_2.services.SessionIDService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import static javax.management.timer.Timer.ONE_MINUTE;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;
import static reactor.util.function.Tuples.of;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"sessionIds"})
public class SessionIDServiceImpl implements SessionIDService {

    private final DllApiService dll;

    @Override
    public Mono<Tuple3<Boolean, String, HttpStatus>> getSessionId(String accountId, String username, String password) {
        return dll.initialiseWithID(accountId)
                  .flatMap(t4 -> {
                      switch (t4.getT1()) {
                          case -1:
                              return just(of(false, t4.getT3(), SERVICE_UNAVAILABLE));

                          case 1: {

                              return dll.logon(t4.getT2(), username, password)
                                        .map(t3 -> {
                                            switch (t3.getT1()) {
                                                case -1:
                                                    return of(false, t3.getT3(), SERVICE_UNAVAILABLE);
                                                case 1:
                                                    return of(true, t3.getT2(), OK);
                                                case 2:
                                                case 4:
                                                    return of(false, t3.getT3(), FORBIDDEN);
                                                default:
                                                    return of(false, t3.getT3(), BAD_REQUEST);

                                            }
                                        });

                          }

                          case 4:
                              return just(of(true, t4.getT2(), OK));

                          default:
                              return just(of(false, t4.getT3(), BAD_REQUEST));
                      }
                  });

    }

    @Override
    @CachePut(key = "{#accountId}")
    public String cache(String accountId, String sessionId) {
        return sessionId;
    }

    @Override
    @Cacheable(key = "{#accountId}")
    public String getCached(String accountId) {
        return "";
    }

    @Scheduled(fixedRate = 55 * ONE_MINUTE)
    @CacheEvict(allEntries = true)
    public void clearCache() {}
}