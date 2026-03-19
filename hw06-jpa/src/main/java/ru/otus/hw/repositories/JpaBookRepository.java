package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    private final EntityManager em;

    @Override
    public Optional<Book> findById(long id) {
        EntityGraph<?> graph = em.getEntityGraph("book-author-genres-graph");
        Map<String, Object> props = Map.of("jakarta.persistence.fetchgraph", graph);
        return Optional.ofNullable(em.find(Book.class, id, props));
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> graph = em.getEntityGraph("book-author-graph");

        return em.createQuery("select b from Book b", Book.class)
                .setHint("jakarta.persistence.fetchgraph", graph)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);

        if (book != null) {
            em.remove(book);
        }
    }
}
