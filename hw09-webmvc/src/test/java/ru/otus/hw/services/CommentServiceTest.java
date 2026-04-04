package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
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

    private Book createTestBook() {
        Author author = new Author(1L, "Author A", null);
        Genre genre = new Genre(1L, "Genre1");

        Book book = new Book(0L, "Test Book for Comments", author, List.of(genre), List.of());
        return bookRepository.save(book);
    }

    @DisplayName("findById должен возвращать комментарий с доступной книгой")
    @Test
    void findById_shouldReturnCommentWithAccessibleBook() {
        Book book = createTestBook();

        Comment inserted = commentService.insert(book.getId(), "Test comment");
        var found = commentService.findById(inserted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("Test comment");
        assertThat(found.get().getBook()).isNotNull();
        assertThat(found.get().getBook().getId()).isEqualTo(book.getId());

        commentService.deleteById(inserted.getId());
        bookRepository.deleteById(book.getId());
    }

    @DisplayName("findAllByBookId должен возвращать все комментарии книги")
    @Test
    void findAllByBookId_shouldReturnComments() {
        Book book = createTestBook();

        var c1 = commentService.insert(book.getId(), "Comment Alpha");
        var c2 = commentService.insert(book.getId(), "Comment Beta");

        List<Comment> comments = commentService.findAllByBookId(book.getId());

        assertThat(comments)
                .hasSize(2)
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Comment Alpha", "Comment Beta");

        commentService.deleteById(c1.getId());
        commentService.deleteById(c2.getId());
        bookRepository.deleteById(book.getId());
    }

    @DisplayName("insert должен сохранять комментарий")
    @Test
    void insert_shouldPersistComment() {
        Book book = createTestBook();

        var saved = commentService.insert(book.getId(), "New comment");

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getText()).isEqualTo("New comment");
        assertThat(saved.getBook().getId()).isEqualTo(book.getId());

        commentService.deleteById(saved.getId());
        bookRepository.deleteById(book.getId());
    }

    @DisplayName("update должен обновлять текст комментария")
    @Test
    void update_shouldUpdateCommentText() {
        Book book = createTestBook();

        var saved = commentService.insert(book.getId(), "Old text");
        var updated = commentService.update(saved.getId(), "New text");

        assertThat(updated.getText()).isEqualTo("New text");
        assertThat(updated.getBook().getId()).isEqualTo(book.getId());

        commentService.deleteById(saved.getId());
        bookRepository.deleteById(book.getId());
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        Book book = createTestBook();

        var saved = commentService.insert(book.getId(), "To be deleted");
        commentService.deleteById(saved.getId());

        assertThat(commentService.findById(saved.getId())).isEmpty();

        bookRepository.deleteById(book.getId());
    }
}
