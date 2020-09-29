package me.povisenko.jacob_within_spring_boot_2.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.povisenko.jacob_within_spring_boot_2.services.DllApiService;
import me.povisenko.jacob_within_spring_boot_2.utils.ResourceUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import static java.lang.Integer.valueOf;
import static me.povisenko.jacob_within_spring_boot_2.services.DllApiService.COM_API_Method.*;
import static me.povisenko.jacob_within_spring_boot_2.utils.LogUtil.logNext;
import static reactor.util.function.Tuples.of;

@Slf4j
@Service
@Profile("local_unix")
public class DllApiServiceMocked implements DllApiService {

    private static final String USER_MATCHES_XML = ResourceUtil.getResourceFileAsString("mocked/UserMatches.xml");
    private static final String FAMILY_NAME = "Smith";

    @Override
    public Mono<Tuple3<Integer, String, String>> initialiseWithID(final String orgId) {
        return Mono.just(of(1, "1", "no error"))// 4 = auto login
                   .doOnEach(logNext(t4 -> log.debug("Mocked {}({}) has been invoked: ", InitialiseWithID, orgId)));
    }

    @Override
    public Mono<Tuple3<Integer, String, String>> logon(final String loginId, final String username, final String password) {
        return Mono.just(t3Response(loginId, loginId))
                   .doOnEach(logNext(t3 -> log.debug("Mocked {}({}, {}, {}) has been invoked", Logon, loginId, username, password)));
    }

    @Override
    public Mono<Tuple3<Integer, String, String>> getMatchedUsers(final String sessionId, final String matchTerm) {
        return Mono.just(matchTerm)
                   .filter(FAMILY_NAME::equals)
                   .map(n -> t3Response(sessionId, USER_MATCHES_XML))
                   .defaultIfEmpty(t3Response("3", "NULL"))
                   .doOnEach(logNext(t3 -> log.debug("Mocked {}({}, {}) has been invoked", getMatchedUsers, sessionId, matchTerm)));
    }

    private Tuple3<Integer, String, String> t3Response(String sessionId, String result) {
        if (Integer.parseInt(sessionId) == 1) {
            assert result != null;
            return of(1, result, "no error");
        }
        return of(valueOf(sessionId), "", "mocked error");
    }
}