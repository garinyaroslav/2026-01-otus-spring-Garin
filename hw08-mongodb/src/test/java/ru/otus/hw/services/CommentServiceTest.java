package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NEVER)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();

        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));

        testBook = bookService.insert(
                "Test Book for Comments",
                author.getId(),
                Set.of(genre.getId()));
    }

    @DisplayName("findById должен возвращать комментарий с доступной книгой")
    @Test
    void findById_shouldReturnCommentWithAccessibleBook() {
        Comment inserted = commentService.insert(testBook.getId(), "Comment 1");

        Optional<Comment> result = commentService.findById(inserted.getId());

        assertThat(result).isPresent();

        Comment actual = result.get();
        assertThat(actual.getText()).isEqualTo("Comment 1");
        assertThat(actual.getBook()).isNotNull();
        assertThat(actual.getBook().getId()).isEqualTo(testBook.getId());
        assertThat(actual.getBook().getTitle()).isEqualTo(testBook.getTitle());
    }

    @DisplayName("findAllByBookId должен возвращать все комментарии для указанной книги")
    @Test
    void findAllByBookId_shouldReturnComments() {
        commentService.insert(testBook.getId(), "First comment");
        commentService.insert(testBook.getId(), "Second comment");
        commentService.insert(testBook.getId(), "Third comment");

        List<Comment> comments = commentService.findAllByBookId(testBook.getId());

        assertThat(comments).hasSize(3);
        assertThat(comments).allSatisfy(comment -> {
            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getText()).isNotBlank();
            assertThat(comment.getBook()).isNotNull();
            assertThat(comment.getBook().getId()).isEqualTo(testBook.getId());
        });
    }

    @DisplayName("insert должен сохранять комментарий и возвращать его с книгой")
    @Test
    void insert_shouldPersistComment() {
        Comment actual = commentService.insert(testBook.getId(), "Great book!");

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getText()).isEqualTo("Great book!");
        assertThat(actual.getBook()).isNotNull();
        assertThat(actual.getBook().getId()).isEqualTo(testBook.getId());
    }

    @DisplayName("update должен обновлять текст комментария")
    @Test
    void update_shouldUpdateCommentText() {
        Comment inserted = commentService.insert(testBook.getId(), "Initial text");

        Comment updated = commentService.update(inserted.getId(), "Updated text");

        assertThat(updated.getId()).isEqualTo(inserted.getId());
        assertThat(updated.getText()).isEqualTo("Updated text");
        assertThat(updated.getBook()).isNotNull();
        assertThat(updated.getBook().getId()).isEqualTo(testBook.getId());
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        Comment inserted = commentService.insert(testBook.getId(), "To be deleted");

        commentService.deleteById(inserted.getId());

        assertThat(commentService.findById(inserted.getId())).isEmpty();
    }
}
