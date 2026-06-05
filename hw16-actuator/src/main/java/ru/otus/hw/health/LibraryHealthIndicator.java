package ru.otus.hw.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;

@Slf4j
@Component("library")
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private static final long MIN_BOOKS_THRESHOLD = 1L;

    private static final long MIN_AUTHORS_THRESHOLD = 1L;

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    @Override
    public Health health() {
        try {
            long bookCount = bookRepository.count();
            long authorCount = authorRepository.count();

            boolean hasNoBooks = bookCount < MIN_BOOKS_THRESHOLD;
            boolean hasNoAuthors = authorCount < MIN_AUTHORS_THRESHOLD;

            if (hasNoBooks || hasNoAuthors) {
                String reason = buildIntegrityReason(hasNoBooks, hasNoAuthors);
                log.warn("Library health DOWN: {}", reason);
                return downHealthWithCounts(reason, bookCount, authorCount);
            }

            return upHealth(bookCount, authorCount);
        } catch (Exception e) {
            log.error("Library health check failed", e);
            return downHealth("Unable to query database: " + e.getMessage());
        }
    }

    private String buildIntegrityReason(boolean noBooks, boolean noAuthors) {
        if (noBooks && noAuthors) {
            return "Data integrity issue: no books and no authors";
        } else if (noBooks) {
            return "Data integrity issue: no books";
        } else {
            return "Data integrity issue: no authors";
        }
    }

    private Health downHealthWithCounts(String reason, long bookCount, long authorCount) {
        return Health.down()
                .withDetail("reason", reason)
                .withDetail("booksCount", bookCount)
                .withDetail("authorsCount", authorCount)
                .build();
    }

    private Health downHealth(String reason) {
        return Health.down()
                .withDetail("reason", reason)
                .build();
    }

    private Health upHealth(long bookCount, long authorCount) {
        return Health.up()
                .withDetail("booksCount", bookCount)
                .withDetail("authorsCount", authorCount)
                .withDetail("status", "Library is operating normally")
                .build();
    }
}
