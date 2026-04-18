package ru.otus.hw.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookRepository bookRepository;

    @DisplayName("findAllByBookId должен возвращать все CommentDto для книги")
    @Test
    void findAllByBookId_shouldReturnCommentDtos() {
        StepVerifier.create(
                bookRepository.save(new Book(null, "Test Book for Comments", 1L))
                        .flatMapMany(book -> commentService.insert(book.getId(), "Comment Alpha")
                                .then(commentService.insert(book.getId(), "Comment Beta"))
                                .thenMany(commentService.findAllByBookId(book.getId()))))
                .assertNext(comment -> assertThat(comment.getText()).isEqualTo("Comment Alpha"))
                .assertNext(comment -> assertThat(comment.getText()).isEqualTo("Comment Beta"))
                .verifyComplete();
    }

    @DisplayName("insert должен сохранять комментарий и возвращать CommentDto")
    @Test
    void insert_shouldPersistAndReturnCommentDto() {

        StepVerifier.create(
                bookRepository.save(new Book(null, "Test Book", 1L))
                        .flatMap(book -> commentService.insert(book.getId(), "New comment")))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isGreaterThan(0);
                    assertThat(saved.getText()).isEqualTo("New comment");
                    assertThat(saved.getBookId()).isGreaterThan(0);
                })
                .verifyComplete();
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        StepVerifier.create(
                bookRepository.save(new Book(null, "Test Book", 1L))
                        .flatMap(book -> commentService.insert(book.getId(), "Comment to delete"))
                        .flatMap(saved -> commentService.deleteById(saved.getId())
                                .thenReturn(saved.getId())))
                .assertNext(commentId -> assertThat(commentId).isGreaterThan(0))
                .verifyComplete();
    }
}
