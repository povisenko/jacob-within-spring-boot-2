package me.povisenko.jacob_within_spring_boot_2.facade;

import me.povisenko.jacob_within_spring_boot_2.facade.handlers.DllApiRouterHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class DllApiRouter {
    @Bean
    public RouterFunction<ServerResponse> dllApiRoute(DllApiRouterHandler handler) {
        return RouterFunctions.route(GET("/api/sessions/{accountId}"), handler::sessionId)
                              .andRoute(GET("/api/users/{matchTerm}"), handler::matchedUsers);
    }
}