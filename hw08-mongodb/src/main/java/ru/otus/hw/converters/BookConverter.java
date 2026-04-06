package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookConverter {

    public String bookToString(Book book) {
        System.out.println(book.getAuthorId());
        String genresString = "";
        if (book.getGenreIds() != null && !book.getGenreIds().isEmpty()) {
            genresString = book.getGenreIds().stream()
                    .map("{%s}"::formatted)
                    .collect(Collectors.joining(", "));
        }

        String authorPart = (book.getAuthorId() != null)
                ? "{" + book.getAuthorId() + "}"
                : "{null}";

        return "Id: %s, title: %s, author: %s, genres: [%s]".formatted(
                book.getId(),
                book.getTitle(),
                authorPart,
                genresString);
    }
}
