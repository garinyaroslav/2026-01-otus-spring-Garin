package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("findById возвращает комментарий с доступной книгой снаружи транзакции")
    void findById_shouldReturnCommentWithAccessibleBook() {
        Optional<Comment> result = commentService.findById(1L);
        assertThat(result).isPresent();

        Comment expected = new Comment();
        expected.setId(1L);
        expected.setText("Comment 1");

        assertThat(result.get())
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(expected);
        assertThat(result.get().getBook()).isNotNull();
        assertThat(result.get().getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllByBookId возвращает комментарии для книги")
    void findAllByBookId_shouldReturnComments() {
        List<Comment> comments = commentService.findAllByBookId(1L);

        assertThat(comments).isNotEmpty();
        assertThat(comments).allSatisfy(c -> {
            assertThat(c.getId()).isGreaterThan(0);
            assertThat(c.getText()).isNotBlank();
        });
    }

    @Test
    @DisplayName("insert сохраняет комментарий")
    void insert_shouldPersistComment() {
        Comment actual = commentService.insert(1L, "Great book!");

        Comment expected = new Comment();
        expected.setText("Great book!");

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .isEqualTo(expected);
        assertThat(actual.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("update обновляет текст комментария")
    void update_shouldUpdateCommentText() {
        Comment inserted = commentService.insert(1L, "Initial text");
        Comment updated = commentService.update(inserted.getId(), "Updated text");

        Comment expected = new Comment();
        expected.setId(inserted.getId());
        expected.setText("Updated text");

        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("deleteById удаляет комментарий")
    void deleteById_shouldDeleteComment() {
        Comment inserted = commentService.insert(1L, "To be deleted");

        commentService.deleteById(inserted.getId());

        assertThat(commentService.findById(inserted.getId())).isEmpty();
    }

}
