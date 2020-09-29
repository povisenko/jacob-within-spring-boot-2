package me.povisenko.jacob_within_spring_boot_2.facade.handlers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.povisenko.jacob_within_spring_boot_2.repo.AccountRepo;
import me.povisenko.jacob_within_spring_boot_2.services.DllApiService;
import me.povisenko.jacob_within_spring_boot_2.services.SessionIDService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static me.povisenko.jacob_within_spring_boot_2.utils.LogUtil.logNext;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DllApiRouterHandler {

    private static final String SESSION_ID_HDR = "X-SESSION-ID";

    private final DllApiService service;
    private final AccountRepo accountRepo;
    private final SessionIDService sessionService;

    public Mono<ServerResponse> sessionId(ServerRequest request) {
        final String accountId = request.pathVariable("accountId");

        return accountRepo.findById(accountId)
                          .flatMap(acc -> Mono.just(!request.queryParam("force")
                                                            .map(Boolean::valueOf)
                                                            .orElse(false))
                                              .filter(Boolean::booleanValue)
                                              .doOnEach(logNext(b -> log.info(format("Trying to find sessionId in cache for account ID %s",//
                                                                                     accountId))))
                                              .map(f -> sessionService.getCached(accountId))
                                              .filter(StringUtils::hasText)
                                              .doOnEach(logNext(t -> log.info("SessionId was found in cache")))
                                              .map(t -> Tuples.of(true, t, OK))
                                              .switchIfEmpty(Mono.just("SessionId was not found in cache. Generating a new one...")
                                                                 .doOnEach(logNext(log::info))
                                                                 .then(sessionService.getSessionId(accountId, acc.getApiUsername(), acc.getApiPassword()))))
                          .doOnEach(logNext(t3 -> {
                              if (t3.getT1()) {
                                  log.info(format("SessionId to return %s", t3.getT2()));
                              } else {
                                  log.warn(format("Session Id could not be retrieved. Cause: %s", t3.getT2()));
                              }
                          }))
                          .doOnNext(t3 -> {
                              if (t3.getT1()) {
                                  log.info("Caching session ID...");
                                  sessionService.cache(accountId, t3.getT2());
                              }
                          })
                          .flatMap(t3 -> status(t3.getT3()).contentType(APPLICATION_JSON)
                                                           .bodyValue(t3.getT1() ? t3.getT2() : Response.error(t3.getT2())))

                          .switchIfEmpty(Mono.just("Account could not be found with provided ID " + accountId)
                                             .doOnEach(logNext(log::info))
                                             .flatMap(msg -> badRequest().bodyValue(Response.error(msg))));
    }

    public Mono<ServerResponse> matchedUsers(ServerRequest request) {

        return sessionIdHeader(request).map(sId -> Tuples.of(sId, request.queryParam("matchTerm")
                                                                         .orElseThrow(() -> new IllegalArgumentException(
                                                                                 "matchTerm query param should be specified"))))
                                       .flatMap(t2 -> service.getMatchedUsers(t2.getT1(), t2.getT2()))
                                       .flatMap(this::handleT3)
                                       .onErrorResume(IllegalArgumentException.class, this::handleIllegalArgumentException);

    }

    private Mono<String> sessionIdHeader(ServerRequest request) {
        return Mono.justOrEmpty(request.headers()
                                       .header(SESSION_ID_HDR)
                                       .stream()
                                       .findFirst()
                                       .orElseThrow(() -> new IllegalArgumentException(SESSION_ID_HDR + " header is mandatory")));
    }

    private Mono<ServerResponse> handleT3(Tuple3<Integer, String, String> t3) {
        switch (t3.getT1()) {
            case 1:
                return ok().contentType(APPLICATION_JSON)
                           .bodyValue(t3.getT2());
            case 2:
                return status(FORBIDDEN).contentType(APPLICATION_JSON)
                                        .bodyValue(Response.error(t3.getT3()));
            default:
                return badRequest().contentType(APPLICATION_JSON)
                                   .bodyValue(Response.error(t3.getT3()));
        }
    }

    private Mono<ServerResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return Mono.just(Response.error(e.getMessage()))
                   .doOnEach(logNext(res -> log.info(String.join(",", res.getErrors()))))
                   .flatMap(res -> badRequest().contentType(MediaType.APPLICATION_JSON)
                                               .bodyValue(res));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response implements Serializable {

        private String message;

        private Set<String> errors;

        private Response(Set<String> errors) {
            this.errors = errors;
        }

        public static Response error(String error) {
            return new Response(singleton(error));
        }
    }
}