package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.services.CommentService;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public String addComment(@RequestParam long bookId, @RequestParam String text) {
        commentService.insert(bookId, text);
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable long id, @RequestParam long bookId) {
        commentService.deleteById(id);
        return "redirect:/books/" + bookId;
    }
}
