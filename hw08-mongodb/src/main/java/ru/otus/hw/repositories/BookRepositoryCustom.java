package ru.otus.hw.repositories;

public interface BookRepositoryCustom {

    void deleteByIdWithCascade(String id);

}
