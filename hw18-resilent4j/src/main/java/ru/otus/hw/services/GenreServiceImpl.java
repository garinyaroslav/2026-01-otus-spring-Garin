package ru.otus.hw.services;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreDto::of)
                .toList();
    }
}
