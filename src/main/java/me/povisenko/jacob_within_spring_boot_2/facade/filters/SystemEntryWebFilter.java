package me.povisenko.jacob_within_spring_boot_2.facade.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static java.util.UUID.randomUUID;
import static reactor.util.context.Context.of;
import static me.povisenko.jacob_within_spring_boot_2.utils.LogUtil.doOnError;
import static me.povisenko.jacob_within_spring_boot_2.utils.LogUtil.logNext;

@Slf4j
@Component
@Order(value = 0)
@RequiredArgsConstructor
public class SystemEntryWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        final ServerHttpRequest request = serverWebExchange.getRequest();

        return Mono.just(request)
                   .doOnEach(logNext(r -> log.info("Inbound request: {} {}", r.getMethod(), r.getURI())))
                   .doOnEach(logNext(r -> log.info("Request headers: {}", r.getHeaders())))

                   .flatMap(r -> enrichResponse(serverWebExchange.getResponse()))

                   .flatMap(r -> webFilterChain.filter(serverWebExchange))

                   .doOnEach(doOnError(Exception.class, e -> {
                       if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatus()
                                                                                                .value() == 404) log.warn(
                               "The requested URI [{}] does not exist", request.getURI());
                       else log.error(e.getMessage(), e);
                   }))

                   .then(debugResponse(serverWebExchange))

                   .subscriberContext(of("trace", randomUUID().toString()));
    }

    private Mono<ServerHttpResponse> enrichResponse(ServerHttpResponse response) {
        return Mono.just(response)
                   .flatMap(r -> Mono.subscriberContext()
                                     .map(ctx -> {
                                         r.getHeaders()
                                          .add("X-Trace", ctx.get("trace"));
                                         return r;
                                     }));
    }

    private Mono<Void> debugResponse(ServerWebExchange serverWebExchange) {
        return log.isDebugEnabled() ? Mono.defer(() -> Mono.just(serverWebExchange.getResponse())
                                                           .doOnEach(logNext(
                                                                   r -> log.debug("Response status code: {}, headers: {}", r.getStatusCode(), r.getHeaders())))
                                                           .then()) : Mono.empty()
                                                                          .then();
    }

}