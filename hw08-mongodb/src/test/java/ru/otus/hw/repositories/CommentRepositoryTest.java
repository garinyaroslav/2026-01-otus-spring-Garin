package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий комментариев")
@ActiveProfiles("test")
@DataMongoTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @DisplayName("должен находить комментарий по id")
    @Test
    void shouldFindById() {
        Book book = mongoTemplate.save(new Book(null, "Test Book", null, List.of()));
        Comment savedComment = mongoTemplate.save(new Comment(null, "Comment 1", book));

        var found = commentRepository.findById(savedComment.getId());

        assertThat(found).isPresent();

        Comment expected = new Comment(null, "Comment 1", new Book(null, "Test Book", null, List.of()));

        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("id", "book.id", "book.author.id", "book.genres.id")
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        Book book = mongoTemplate.save(new Book(null, "Test Book", null, List.of()));

        mongoTemplate.save(new Comment(null, "Comment 1", book));
        mongoTemplate.save(new Comment(null, "Comment 2", book));

        List<Comment> found = commentRepository.findAllByBookId(book.getId());

        assertThat(found).hasSize(2);

        List<Comment> expected = List.of(
                new Comment(null, "Comment 1", new Book(null, "Test Book", null, List.of())),
                new Comment(null, "Comment 2", new Book(null, "Test Book", null, List.of())));

        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("id", "book.id", "book.author.id", "book.genres.id")
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }
}
