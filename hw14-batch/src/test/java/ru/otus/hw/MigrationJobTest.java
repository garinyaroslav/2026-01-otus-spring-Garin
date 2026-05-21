package ru.otus.hw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import ru.otus.hw.mongoModels.AuthorMongo;
import ru.otus.hw.mongoModels.BookMongo;
import ru.otus.hw.mongoModels.CommentMongo;
import ru.otus.hw.mongoModels.GenreMongo;
import ru.otus.hw.mongoRepositories.AuthorMongoRepository;
import ru.otus.hw.mongoRepositories.BookMongoRepository;
import ru.otus.hw.mongoRepositories.CommentMongoRepository;
import ru.otus.hw.mongoRepositories.GenreMongoRepository;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@SpringBatchTest
@Testcontainers
class MigrationJobTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private AuthorMongoRepository authorMongoRepository;

    @Autowired
    private GenreMongoRepository genreMongoRepository;

    @Autowired
    private BookMongoRepository bookMongoRepository;

    @Autowired
    private CommentMongoRepository commentMongoRepository;

    @BeforeEach
    void cleanMongo() {
        commentMongoRepository.deleteAll();
        bookMongoRepository.deleteAll();
        genreMongoRepository.deleteAll();
        authorMongoRepository.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("Полный прогон job: все 4 шага должны завершиться COMPLETED")
    void migrationJobShouldComplete() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        execution.getStepExecutions().forEach(
                step -> assertThat(step.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode()));
    }

    @Test
    @DisplayName("Авторы мигрированы корректно")
    void authorsShouldBeMigrated() throws Exception {
        jobLauncherTestUtils.launchJob();

        List<AuthorMongo> authors = authorMongoRepository.findAll();
        assertThat(authors).hasSize(3);
        assertThat(authors).extracting(AuthorMongo::getFullName)
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }

    @Test
    @DisplayName("Жанры мигрированы корректно")
    void genresShouldBeMigrated() throws Exception {
        jobLauncherTestUtils.launchJob();

        List<GenreMongo> genres = genreMongoRepository.findAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    @DisplayName("Книги мигрированы с корректными ссылками на автора и жанры")
    void booksShouldBeMigratedWithRelations() throws Exception {
        jobLauncherTestUtils.launchJob();

        List<BookMongo> books = bookMongoRepository.findAll();
        assertThat(books).hasSize(3);

        books.forEach(book -> {
            assertThat(book.getAuthorId())
                    .as("Book '%s' should have authorId", book.getTitle())
                    .isNotNull();
            assertThat(book.getGenreIds())
                    .as("Book '%s' should have genreIds", book.getTitle())
                    .isNotEmpty()
                    .hasSize(2);
        });

        books.forEach(book -> assertThat(authorMongoRepository.findById(book.getAuthorId())).isPresent());
    }

    @Test
    @DisplayName("Комментарии мигрированы с корректными ссылками на книги")
    void commentsShouldBeMigratedWithBookRelations() throws Exception {
        jobLauncherTestUtils.launchJob();

        List<CommentMongo> comments = commentMongoRepository.findAll();
        assertThat(comments).hasSize(4);

        comments.forEach(comment -> {
            assertThat(comment.getBookId())
                    .as("Comment '%s' should have bookId", comment.getText())
                    .isNotNull();
            assertThat(bookMongoRepository.findById(comment.getBookId())).isPresent();
        });
    }

    @Test
    @DisplayName("Шаг migrateAuthorsStep записал правильное количество записей")
    void authorsStepShouldWriteCorrectCount() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchStep("migrateAuthorsStep");

        StepExecution stepExecution = execution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getWriteCount()).isEqualTo(3);
        assertThat(stepExecution.getReadCount()).isEqualTo(3);
    }
}
