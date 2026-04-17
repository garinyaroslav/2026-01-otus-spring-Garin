package ru.otus.hw.routers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import ru.otus.hw.handlers.AuthorHandler;
import ru.otus.hw.handlers.BookHandler;
import ru.otus.hw.handlers.CommentHandler;
import ru.otus.hw.handlers.GenreHandler;

@Configuration
public class RoutersConfig {

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

    @Bean
    public RouterFunction<ServerResponse> authorRoutes(AuthorHandler handler) {
        return RouterFunctions.route()
                .GET("/api/authors", accept(APPLICATION_JSON), handler::list)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> genreRoutes(GenreHandler handler) {
        return RouterFunctions.route()
                .GET("/api/genres", accept(APPLICATION_JSON), handler::list)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> commentRoutes(CommentHandler handler) {
        return RouterFunctions.route()
                .POST("/api/comments", accept(APPLICATION_JSON), handler::add)
                .DELETE("/api/comments/{id}", handler::delete)
                .build();
    }

}
