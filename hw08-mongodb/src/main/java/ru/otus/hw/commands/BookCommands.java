package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({ "SpellCheckingInspection", "unused" })
@RequiredArgsConstructor
@ShellComponent
public class BookCommands {

    private final BookService bookService;

    private final BookConverter bookConverter;

    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        return bookService.findAll().stream()
                .map(bookConverter::bookToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find book by id", key = "bbid")
    public String findBookById(String id) {
        return bookService.findById(id)
                .map(bookConverter::bookToString)
                .orElse("Book with id %d not found".formatted(id));
    }

    // bins newBook 69c7ce5ce4a44e2949fd2b7f 69c7cdfce4a44e2949fd2b79
    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(String title, String authorId, Set<String> genresIds) {
        var savedBook = bookService.insert(title, authorId, genresIds);
        return bookConverter.bookToString(savedBook);
    }

    // bupd 69c7d39ac9e8cebcb907e4c9 editedBook 69c7ce70e4a44e2949fd2b81
    // 69c7ce08e4a44e2949fd2b7b,69c7ce0ee4a44e2949fd2b7d
    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(String id, String title, String authorId, Set<String> genresIds) {
        var savedBook = bookService.update(id, title, authorId, genresIds);
        return bookConverter.bookToString(savedBook);
    }

    // bupd 69c7d39ac9e8cebcb907e4c9
    @ShellMethod(value = "Delete book by id", key = "bdel")
    public void deleteBook(String id) {
        bookService.deleteById(id);
    }
}
