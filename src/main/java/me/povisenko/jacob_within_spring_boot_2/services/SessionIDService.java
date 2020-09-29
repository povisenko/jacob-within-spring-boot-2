package me.povisenko.jacob_within_spring_boot_2.services;

import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

public interface SessionIDService {

    /**
     * @param accountId identifier for which we retrieve SessionID
     * @param username
     * @param password
     * @return Tuple3 containing the following values:
     * result ( Boolean), sessionId (String) and status (HTTP Status depending on the result)
     */
    Mono<Tuple3<Boolean, String, HttpStatus>> getSessionId(String accountId, String username, String password);

    String cache(String accountId, String sessionId);

    String getCached(String accountId);
}