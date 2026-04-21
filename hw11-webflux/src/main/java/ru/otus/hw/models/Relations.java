package ru.otus.hw.models;

import java.util.List;

public record Relations(long authorId, List<Long> genreIds) {
}
