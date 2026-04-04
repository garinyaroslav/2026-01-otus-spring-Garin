package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.HashSet;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        model.addAttribute("book", book);
        return "books/view";
    }

    @GetMapping("/create")
    public String createBookForm(Model model) {
        model.addAttribute("book", new BookDto());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "books/form";
    }

    @PostMapping("/create")
    public String createBook(@ModelAttribute("book") BookDto dto) {
        bookService.insert(dto.getTitle(), dto.getAuthorId(), new HashSet<>(dto.getGenreIds()));

        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        var dto = new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenres().stream().map(g -> g.getId()).toList());

        model.addAttribute("book", dto);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "books/form";
    }

    @PostMapping("/{id}/edit")
    public String editBook(@PathVariable long id, @ModelAttribute("book") BookDto dto) {
        bookService.update(id, dto.getTitle(), dto.getAuthorId(), new HashSet<>(dto.getGenreIds()));

        return "redirect:/books";
    }

    @GetMapping("/{id}/delete")
    public String deleteBookForm(@PathVariable long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        model.addAttribute("book", book);
        return "books/delete";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(id);

        return "redirect:/books";
    }
}
