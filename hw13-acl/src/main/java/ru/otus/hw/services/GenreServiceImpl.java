package ru.otus.hw.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreDto::of)
                .toList();
    }
}
