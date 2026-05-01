package ru.otus.hw.services;

import java.util.List;

import ru.otus.hw.dto.AuthorDto;

public interface AuthorService {

    List<AuthorDto> findAll();

}
