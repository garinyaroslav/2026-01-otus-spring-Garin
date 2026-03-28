package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        deleteAllData();
        testBook = bookService.insert("Test Book for Comments", 1L, Set.of(1L));
    }

    private void deleteAllData() {
        bookService.findAll().forEach(book -> {
            commentService.findAllByBookId(book.getId())
                    .forEach(comment -> commentService.deleteById(comment.getId()));
        });

        bookService.findAll().forEach(book -> bookService.deleteById(book.getId()));
    }

    @DisplayName("findById должен возвращать комментарий с доступной книгой снаружи транзакции")
    @Test
    void findById_shouldReturnCommentWithAccessibleBook() {
        Comment inserted = commentService.insert(testBook.getId(), "Comment 1");

        Optional<Comment> result = commentService.findById(inserted.getId());

        assertThat(result)
                .as("Комментарий должен быть найден")
                .isPresent();

        Comment actual = result.get();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .isEqualTo(new Comment(0L, "Comment 1", null));

        assertThat(actual.getBook()).isNotNull();
        assertThat(actual.getBook().getId()).isEqualTo(testBook.getId());
    }

    @DisplayName("findAllByBookId должен возвращать комментарии для книги")
    @Test
    void findAllByBookId_shouldReturnComments() {
        commentService.insert(testBook.getId(), "First comment");
        commentService.insert(testBook.getId(), "Second comment");
        commentService.insert(testBook.getId(), "Third comment");

        List<Comment> comments = commentService.findAllByBookId(testBook.getId());

        assertThat(comments).hasSize(3);
        assertThat(comments).allSatisfy(comment -> {
            assertThat(comment.getId()).isGreaterThan(0);
            assertThat(comment.getText()).isNotBlank();
            assertThat(comment.getBook()).isNotNull();
            assertThat(comment.getBook().getId()).isEqualTo(testBook.getId());
        });
    }

    @DisplayName("insert должен сохранять комментарий")
    @Test
    void insert_shouldPersistComment() {
        Comment actual = commentService.insert(testBook.getId(), "Great book!");

        assertThat(actual.getId()).isGreaterThan(0);
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
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        Comment inserted = commentService.insert(testBook.getId(), "To be deleted");

        commentService.deleteById(inserted.getId());

        assertThat(commentService.findById(inserted.getId())).isEmpty();
    }
}
