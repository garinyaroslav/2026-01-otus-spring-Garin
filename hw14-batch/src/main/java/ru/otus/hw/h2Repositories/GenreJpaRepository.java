package ru.otus.hw.h2Repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.otus.hw.h2Models.GenreJpa;

public interface GenreJpaRepository extends JpaRepository<GenreJpa, Long> {

    List<GenreJpa> findAllByIdIn(Set<Long> ids);

}
