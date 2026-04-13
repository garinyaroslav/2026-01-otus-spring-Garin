package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

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
        model.addAttribute("book", bookService.findById(id));

        return "books/view";
    }

    @GetMapping("/create")
    public String createBookForm(Model model) {
        model.addAttribute("bookCreateDto", new BookCreateDto());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "books/form-create";
    }

    @PostMapping("/create")
    public String createBook(@Valid @ModelAttribute("bookCreateDto") BookCreateDto dto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            return "books/form-create";
        }

        bookService.insert(dto);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable long id, Model model) {
        var book = bookService.findById(id);

        var dto = new BookUpdateDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenres().stream().map(g -> g.getId()).collect(Collectors.toSet()));

        model.addAttribute("bookUpdateDto", dto);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "books/form-edit";
    }

    @PostMapping("/{id}/edit")
    public String editBook(@PathVariable long id,
            @Valid @ModelAttribute("bookUpdateDto") BookUpdateDto dto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            return "books/form-edit";
        }

        bookService.update(dto);
        return "redirect:/books";
    }

    @GetMapping("/{id}/delete")
    public String deleteBookForm(@PathVariable long id, Model model) {
        model.addAttribute("book", bookService.findById(id));

        return "books/delete";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(id);

        return "redirect:/books";
    }
}
