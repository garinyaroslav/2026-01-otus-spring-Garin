package ru.otus.hw.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.BookHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BookRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> bookRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .GET("/api/books", accept(APPLICATION_JSON), handler::list)
                .GET("/api/books/{id}", accept(APPLICATION_JSON), handler::getById)
                .POST("/api/books", accept(APPLICATION_JSON), handler::create)
                .PUT("/api/books/{id}", accept(APPLICATION_JSON), handler::update)
                .DELETE("/api/books/{id}", handler::delete)
                .build();
    }

}
