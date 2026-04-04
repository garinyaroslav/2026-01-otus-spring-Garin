package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookRepository bookRepository;

    private static final long EXISTING_BOOK_ID = 1L;

    @DisplayName("findById должен возвращать комментарий с доступной книгой")
    @Test
    void findById_shouldReturnCommentWithAccessibleBook() {
        assertThat(bookRepository.findById(EXISTING_BOOK_ID)).isPresent();

        var inserted = commentService.insert(EXISTING_BOOK_ID, "Test comment");
        var found = commentService.findById(inserted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("Test comment");
        assertThat(found.get().getBook()).isNotNull();
        assertThat(found.get().getBook().getId()).isEqualTo(EXISTING_BOOK_ID);

        commentService.deleteById(inserted.getId());
    }

    @DisplayName("findAllByBookId должен возвращать все комментарии книги")
    @Test
    void findAllByBookId_shouldReturnComments() {
        var c1 = commentService.insert(EXISTING_BOOK_ID, "Comment Alpha");
        var c2 = commentService.insert(EXISTING_BOOK_ID, "Comment Beta");

        List<Comment> comments = commentService.findAllByBookId(EXISTING_BOOK_ID);

        assertThat(comments)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(Comment::getText)
                .contains("Comment Alpha", "Comment Beta");

        commentService.deleteById(c1.getId());
        commentService.deleteById(c2.getId());
    }

    @DisplayName("insert должен сохранять комментарий")
    @Test
    void insert_shouldPersistComment() {
        var saved = commentService.insert(EXISTING_BOOK_ID, "New comment");

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getText()).isEqualTo("New comment");
        assertThat(saved.getBook().getId()).isEqualTo(EXISTING_BOOK_ID);

        commentService.deleteById(saved.getId());
    }

    @DisplayName("update должен обновлять текст комментария")
    @Test
    void update_shouldUpdateCommentText() {
        var saved = commentService.insert(EXISTING_BOOK_ID, "Old text");
        var updated = commentService.update(saved.getId(), "New text");

        assertThat(updated.getText()).isEqualTo("New text");
        assertThat(updated.getBook().getId()).isEqualTo(EXISTING_BOOK_ID);

        commentService.deleteById(saved.getId());
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        var saved = commentService.insert(EXISTING_BOOK_ID, "To be deleted");
        commentService.deleteById(saved.getId());

        assertThat(commentService.findById(saved.getId())).isEmpty();
    }
}
