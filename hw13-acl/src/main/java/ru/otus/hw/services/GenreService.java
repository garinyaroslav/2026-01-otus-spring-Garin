package ru.otus.hw.services;

import java.util.List;

import ru.otus.hw.dto.GenreDto;

public interface GenreService {

    List<GenreDto> findAll();

}
