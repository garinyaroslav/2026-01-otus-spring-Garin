package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий комментариев")
@DataJpaTest
@Import(JpaCommentRepository.class)
class JpaCommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaCommentRepository commentRepository;

    private Book book;

    @BeforeEach
    void setUp() {
        var author = em.persistAndFlush(new Author(0, "Author", null));
        var genre = em.persistAndFlush(new Genre(0, "Genre"));
        book = em.persistAndFlush(new Book(0, "Book", author, List.of(genre), List.of()));
    }

    @DisplayName("должен находить комментарий по id")
    @Test
    void shouldFindById() {
        var comment = em.persistAndFlush(new Comment(0, "Hello", book));

        var found = commentRepository.findById(comment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("Hello");
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        em.persistAndFlush(new Comment(0, "Comment 1", book));
        em.persistAndFlush(new Comment(0, "Comment 2", book));

        List<Comment> comments = commentRepository.findAllByBookId(book.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getText)
                .containsExactlyInAnyOrder("Comment 1", "Comment 2");
    }

    @DisplayName("должен возвращать пустой список для книги без комментариев")
    @Test
    void shouldReturnEmptyForBookWithNoComments() {
        var author2 = em.persistAndFlush(new Author(0, "Author2", null));
        var genre2 = em.persistAndFlush(new Genre(0, "Genre2"));
        var emptyBook = em.persistAndFlush(new Book(0, "Empty", author2, List.of(genre2), List.of()));

        assertThat(commentRepository.findAllByBookId(emptyBook.getId())).isEmpty();
    }
}
