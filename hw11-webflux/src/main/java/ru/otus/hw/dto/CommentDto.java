package ru.otus.hw.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private Long bookId;

    public static CommentDto of(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getBookId());
    }

    public static List<CommentDto> fromList(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(CommentDto::of)
                .toList();
    }

    public static Flux<CommentDto> fromList(Flux<Comment> comments) {
        if (comments == null) {
            return Flux.empty();
        }

        return comments.map(CommentDto::of);
    }

}
