package ru.otus.hw.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.models.Genre;

@Repository
@RequiredArgsConstructor
public class JpaGenreRepository implements GenreRepository {

    private final EntityManager em;

    @Override
    public List<Genre> findAll() {
        return em.createQuery("select g from Genre g", Genre.class).getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return em.createQuery("select g from Genre g where g.id in :ids", Genre.class)
                .setParameter("ids", ids)
                .getResultList();
    }

}
