package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий комментариев")
@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentRepository commentRepository;

    @DisplayName("должен находить комментарий по id")
    @Test
    void shouldFindById() {
        var expected = new Comment(1L, "Comment 1", em.find(Book.class, 1L));

        var found = commentRepository.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("book.author.books", "book.genres.books")
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        var book = em.find(Book.class, 1L);
        var expected = List.of(
                new Comment(1L, "Comment 1", book),
                new Comment(2L, "Comment 2", book));

        var found = commentRepository.findAllByBookId(1L);
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("book.author.books", "book.genres.books")
                .isEqualTo(expected);
    }

    @DisplayName("должен возвращать пустой список для книги без комментариев")
    @Test
    void shouldReturnEmptyForBookWithNoComments() {
        assertThat(commentRepository.findAllByBookId(999L)).isEmpty();
    }

}
