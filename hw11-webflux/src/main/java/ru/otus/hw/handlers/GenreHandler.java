package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

@Component
@RequiredArgsConstructor
public class GenreHandler {

    private final GenreService genreService;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok()
                .body(genreService.findAll(), GenreDto.class);
    }
}
