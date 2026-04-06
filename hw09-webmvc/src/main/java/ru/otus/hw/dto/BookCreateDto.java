package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateDto {

    @NotBlank(message = "Title is required")
    @Max(value = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotNull(message = "Author is required")
    @Positive(message = "Invalid author selection")
    private Long authorId;

    @NotEmpty(message = "At least one genre must be selected")
    private Set<Long> genreIds;

}
