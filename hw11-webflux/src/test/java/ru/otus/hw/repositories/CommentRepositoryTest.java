package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;

@DisplayName("Репозиторий комментариев")
@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @DisplayName("должен находить комментарий по id")
    @Test
    void shouldFindById() {
        StepVerifier.create(commentRepository.findById(1L))
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getText()).isEqualTo("Comment 1");
                    assertThat(found.getBookId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        StepVerifier.create(commentRepository.findAllByBookId(1L))
                .assertNext(comment -> {
                    assertThat(comment.getId()).isEqualTo(1L);
                    assertThat(comment.getText()).isEqualTo("Comment 1");
                })
                .assertNext(comment -> {
                    assertThat(comment.getId()).isEqualTo(2L);
                    assertThat(comment.getText()).isEqualTo("Comment 2");
                })
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой список для книги без комментариев")
    @Test
    void shouldReturnEmptyForBookWithNoComments() {
        StepVerifier.create(commentRepository.findAllByBookId(999L))
                .verifyComplete();
    }
}
