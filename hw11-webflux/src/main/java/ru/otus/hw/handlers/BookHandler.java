package ru.otus.hw.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.ErrorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

@Component
@RequiredArgsConstructor
public class BookHandler {

    private final BookService bookService;

    private final Validator validator;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok()
                .body(bookService.findAll(), BookDto.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return bookService.findById(id)
                .flatMap(book -> ServerResponse.ok().bodyValue(book))
                .onErrorResume(EntityNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(BookCreateDto.class)
                .flatMap(dto -> validate(dto, BookCreateDto.class)
                        .switchIfEmpty(
                                bookService.insert(dto)
                                        .flatMap(created -> ServerResponse.status(HttpStatus.CREATED)
                                                .bodyValue(created))));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(BookUpdateDto.class)
                .flatMap(dto -> {
                    dto.setId(id);
                    return validate(dto, BookUpdateDto.class)
                            .switchIfEmpty(
                                    bookService.update(dto)
                                            .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                                            .onErrorResume(EntityNotFoundException.class,
                                                    e -> ServerResponse.notFound().build()));
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return bookService.deleteById(id)
                .then(ServerResponse.noContent().build());
    }

    private <T> Mono<ServerResponse> validate(T dto, Class<T> clazz) {
        var errors = new BeanPropertyBindingResult(dto, clazz.getSimpleName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            errors.getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
            return ServerResponse.badRequest()
                    .bodyValue(new ErrorDto("Validation failed", fieldErrors));
        }
        return Mono.empty();
    }
}
