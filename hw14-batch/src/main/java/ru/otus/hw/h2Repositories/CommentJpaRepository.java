package ru.otus.hw.h2Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.otus.hw.h2Models.CommentJpa;

public interface CommentJpaRepository extends JpaRepository<CommentJpa, Long> {

    List<CommentJpa> findAllByBookId(long bookId);

}
