package ru.otus.hw.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.GenreHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class GenreRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> genreRoutes(GenreHandler handler) {
        return RouterFunctions.route()
                .GET("/api/genres", accept(APPLICATION_JSON), handler::list)
                .build();
    }

}
