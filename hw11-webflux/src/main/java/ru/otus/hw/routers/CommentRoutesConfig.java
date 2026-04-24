package ru.otus.hw.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.CommentHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class CommentRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> commentRoutes(CommentHandler handler) {
        return RouterFunctions.route()
                .POST("/api/comments", accept(APPLICATION_JSON), handler::add)
                .DELETE("/api/comments/{id}", handler::delete)
                .build();
    }

}
