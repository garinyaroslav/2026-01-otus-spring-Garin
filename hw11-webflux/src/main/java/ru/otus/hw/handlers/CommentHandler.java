package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.ErrorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CommentService;

@Component
@RequiredArgsConstructor
public class CommentHandler {

    private final CommentService commentService;

    public Mono<ServerResponse> add(ServerRequest request) {
        String bookIdParam = request.queryParam("bookId").orElse("");
        String text = request.queryParam("text").orElse("").trim();

        if (bookIdParam.isBlank() || text.isBlank()) {
            return ServerResponse.badRequest()
                    .bodyValue(new ErrorDto("bookId and text are required", null));
        }

        long bookId;
        try {
            bookId = Long.parseLong(bookIdParam);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .bodyValue(new ErrorDto("bookId must be a number", null));
        }

        return commentService.insert(bookId, text)
                .flatMap(created -> ServerResponse.status(HttpStatus.CREATED).bodyValue(created))
                .onErrorResume(EntityNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return commentService.deleteById(id)
                .then(ServerResponse.noContent().build());
    }
}
