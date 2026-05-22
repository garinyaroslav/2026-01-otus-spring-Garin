package ru.otus.hw.h2Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.otus.hw.h2Models.AuthorJpa;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpa, Long> {
}
