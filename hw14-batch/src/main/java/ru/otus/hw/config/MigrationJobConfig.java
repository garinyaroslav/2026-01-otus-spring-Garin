package ru.otus.hw.config;

import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.h2Models.AuthorJpa;
import ru.otus.hw.h2Models.BookJpa;
import ru.otus.hw.h2Models.CommentJpa;
import ru.otus.hw.h2Models.GenreJpa;
import ru.otus.hw.h2Repositories.AuthorJpaRepository;
import ru.otus.hw.h2Repositories.BookJpaRepository;
import ru.otus.hw.h2Repositories.CommentJpaRepository;
import ru.otus.hw.h2Repositories.GenreJpaRepository;
import ru.otus.hw.mongoModels.AuthorMongo;
import ru.otus.hw.mongoModels.BookMongo;
import ru.otus.hw.mongoModels.CommentMongo;
import ru.otus.hw.mongoModels.GenreMongo;
import ru.otus.hw.mongoRepositories.AuthorMongoRepository;
import ru.otus.hw.mongoRepositories.BookMongoRepository;
import ru.otus.hw.mongoRepositories.GenreMongoRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MigrationJobConfig {

    public static final String MIGRATION_JOB_NAME = "migrationJob";

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final MongoTemplate mongoTemplate;

    private final AuthorJpaRepository authorJpaRepository;

    private final GenreJpaRepository genreJpaRepository;

    private final BookJpaRepository bookJpaRepository;

    private final CommentJpaRepository commentJpaRepository;

    private final AuthorMongoRepository authorMongoRepository;

    private final GenreMongoRepository genreMongoRepository;

    private final BookMongoRepository bookMongoRepository;

    @Bean
    public Job migrationJob(
            Step migrateAuthorsStep,
            Step migrateGenresStep,
            Step migrateBooksStep,
            Step migrateCommentsStep) {
        return new JobBuilder(MIGRATION_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(splitAuthorsAndGenres(migrateAuthorsStep, migrateGenresStep))
                .next(migrateBooksStep)
                .next(migrateCommentsStep)
                .end()
                .listener(migrationJobListener())
                .build();
    }

    private Flow splitAuthorsAndGenres(Step migrateAuthorsStep, Step migrateGenresStep) {
        return new FlowBuilder<SimpleFlow>("splitAuthorsAndGenresFlow")
                .split(new SimpleAsyncTaskExecutor("migration-"))
                .add(
                        new FlowBuilder<SimpleFlow>("authorsFlow")
                                .start(migrateAuthorsStep).build(),
                        new FlowBuilder<SimpleFlow>("genresFlow")
                                .start(migrateGenresStep).build())
                .build();
    }

    @Bean
    public JobExecutionListener migrationJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("Starting migration job...");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("Migration completed successfully!");
                } else {
                    log.error("Migration finished with status: {}", jobExecution.getStatus());
                }
            }
        };
    }

    @Bean
    public Step migrateAuthorsStep(
            ItemReader<AuthorJpa> authorReader,
            ItemProcessor<AuthorJpa, AuthorMongo> authorProcessor,
            ItemWriter<AuthorMongo> authorWriter) {
        return new StepBuilder("migrateAuthorsStep", jobRepository)
                .<AuthorJpa, AuthorMongo>chunk(CHUNK_SIZE, transactionManager)
                .reader(authorReader)
                .processor(authorProcessor)
                .writer(authorWriter)
                .listener(stepListener("Authors"))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<AuthorJpa> authorReader() {
        List<AuthorJpa> authors = authorJpaRepository.findAll();
        log.info("Read {} authors from H2", authors.size());
        return new ListItemReader<>(authors);
    }

    @Bean
    public ItemProcessor<AuthorJpa, AuthorMongo> authorProcessor() {
        return source -> {
            AuthorMongo target = new AuthorMongo();
            target.setFullName(source.getFullName());
            target.setSourceId(source.getId());
            return target;
        };
    }

    @Bean
    public MongoItemWriter<AuthorMongo> authorWriter() {
        return new MongoItemWriterBuilder<AuthorMongo>()
                .template(mongoTemplate)
                .collection("authors")
                .build();
    }

    @Bean
    public Step migrateGenresStep(
            ItemReader<GenreJpa> genreReader,
            ItemProcessor<GenreJpa, GenreMongo> genreProcessor,
            ItemWriter<GenreMongo> genreWriter) {
        return new StepBuilder("migrateGenresStep", jobRepository)
                .<GenreJpa, GenreMongo>chunk(CHUNK_SIZE, transactionManager)
                .reader(genreReader)
                .processor(genreProcessor)
                .writer(genreWriter)
                .listener(stepListener("Genres"))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<GenreJpa> genreReader() {
        List<GenreJpa> genres = genreJpaRepository.findAll();
        log.info("Read {} genres from H2", genres.size());
        return new ListItemReader<>(genres);
    }

    @Bean
    public ItemProcessor<GenreJpa, GenreMongo> genreProcessor() {
        return source -> {
            GenreMongo target = new GenreMongo();
            target.setName(source.getName());
            target.setSourceId(source.getId());
            return target;
        };
    }

    @Bean
    public MongoItemWriter<GenreMongo> genreWriter() {
        return new MongoItemWriterBuilder<GenreMongo>()
                .template(mongoTemplate)
                .collection("genres")
                .build();
    }

    @Bean
    public Step migrateBooksStep(
            ItemReader<BookJpa> bookReader,
            ItemProcessor<BookJpa, BookMongo> bookProcessor,
            ItemWriter<BookMongo> bookWriter) {
        return new StepBuilder("migrateBooksStep", jobRepository)
                .<BookJpa, BookMongo>chunk(CHUNK_SIZE, transactionManager)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .listener(stepListener("Books"))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<BookJpa> bookReader() {
        List<BookJpa> books = bookJpaRepository.findAll();
        log.info("Read {} books from H2", books.size());
        return new ListItemReader<>(books);
    }

    @Bean
    public ItemProcessor<BookJpa, BookMongo> bookProcessor() {
        return source -> {
            BookMongo target = new BookMongo();
            target.setTitle(source.getTitle());
            target.setSourceId(source.getId());

            authorMongoRepository.findBySourceId(source.getAuthor().getId())
                    .ifPresentOrElse(
                            mongoAuthor -> target.setAuthorId(mongoAuthor.getId()),
                            () -> log.warn("Author with sourceId={} not found in MongoDB", source.getAuthor().getId()));

            List<String> mongoGenreIds = source.getGenres().stream()
                    .map(genreJpa -> genreMongoRepository.findBySourceId(genreJpa.getId())
                            .map(GenreMongo::getId)
                            .orElseGet(() -> {
                                log.warn("Genre with sourceId={} not found in MongoDB", genreJpa.getId());
                                return null;
                            }))
                    .filter(id -> id != null)
                    .toList();

            target.setGenreIds(mongoGenreIds);
            return target;
        };
    }

    @Bean
    public MongoItemWriter<BookMongo> bookWriter() {
        return new MongoItemWriterBuilder<BookMongo>()
                .template(mongoTemplate)
                .collection("books")
                .build();
    }

    @Bean
    public Step migrateCommentsStep(
            ItemReader<CommentJpa> commentReader,
            ItemProcessor<CommentJpa, CommentMongo> commentProcessor,
            ItemWriter<CommentMongo> commentWriter) {
        return new StepBuilder("migrateCommentsStep", jobRepository)
                .<CommentJpa, CommentMongo>chunk(CHUNK_SIZE, transactionManager)
                .reader(commentReader)
                .processor(commentProcessor)
                .writer(commentWriter)
                .listener(stepListener("Comments"))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<CommentJpa> commentReader() {
        List<CommentJpa> comments = commentJpaRepository.findAll();
        log.info("Read {} comments from H2", comments.size());
        return new ListItemReader<>(comments);
    }

    @Bean
    public ItemProcessor<CommentJpa, CommentMongo> commentProcessor() {
        return source -> {
            CommentMongo target = new CommentMongo();
            target.setText(source.getText());

            bookMongoRepository.findBySourceId(source.getBook().getId())
                    .ifPresentOrElse(
                            mongoBook -> target.setBookId(mongoBook.getId()),
                            () -> log.warn("Book with sourceId={} not found in MongoDB", source.getBook().getId()));

            return target;
        };
    }

    @Bean
    public MongoItemWriter<CommentMongo> commentWriter() {
        return new MongoItemWriterBuilder<CommentMongo>()
                .template(mongoTemplate)
                .collection("comments")
                .build();
    }

    private StepExecutionListener stepListener(String entityName) {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting migration of {}...", entityName);
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Finished migration of {}: read={}, written={}, skipped={}",
                        entityName,
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount());
                return stepExecution.getExitStatus();
            }
        };
    }
}
