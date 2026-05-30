package ru.otus.hw.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;

@Component("library")
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private static final long MIN_BOOKS_THRESHOLD = 1L;

    private static final long MIN_AUTHORS_THRESHOLD = 1L;

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    @Override
    public Health health() {
        long bookCount = bookRepository.count();
        long authorCount = authorRepository.count();
        if (bookCount < MIN_BOOKS_THRESHOLD) {
            return Health.down()
                    .withDetail("reason", "Library has no books — data integrity issue")
                    .withDetail("booksCount", bookCount)
                    .withDetail("authorsCount", authorCount)
                    .build();
        }
        if (authorCount < MIN_AUTHORS_THRESHOLD) {
            return Health.down()
                    .withDetail("reason", "Library has no authors — data integrity issue")
                    .withDetail("booksCount", bookCount)
                    .withDetail("authorsCount", authorCount)
                    .build();
        }

        return Health.up()
                .withDetail("booksCount", bookCount)
                .withDetail("authorsCount", authorCount)
                .withDetail("status", "Library is operating normally")
                .build();
    }
}
