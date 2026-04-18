package ru.otus.hw.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.AuthorHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class AuthorRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> authorRoutes(AuthorHandler handler) {
        return RouterFunctions.route()
                .GET("/api/authors", accept(APPLICATION_JSON), handler::list)
                .build();
    }

}
