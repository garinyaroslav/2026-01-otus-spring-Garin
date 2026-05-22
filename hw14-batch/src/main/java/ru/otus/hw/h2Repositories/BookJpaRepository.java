package ru.otus.hw.h2Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.otus.hw.h2Models.BookJpa;

public interface BookJpaRepository extends JpaRepository<BookJpa, Long> {

    @Override
    @EntityGraph(value = "book-author-genres-graph")
    Optional<BookJpa> findById(Long id);

    @EntityGraph(value = "book-author-graph")
    List<BookJpa> findAll();

}
