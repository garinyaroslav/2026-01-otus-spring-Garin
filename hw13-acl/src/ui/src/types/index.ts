export interface AuthorDto {
    id: number;
    fullName: string;
}

export interface GenreDto {
    id: number;
    name: string;
}

export interface CommentDto {
    id: number;
    text: string;
    bookId: number;
}

export interface BookDto {
    id: number;
    title: string;
    author: AuthorDto;
    genres: GenreDto[];
    comments: CommentDto[];
}

export interface BookCreateDto {
    title: string;
    authorId: number;
    genreIds: number[];
}

export interface BookUpdateDto {
    id: number;
    title: string;
    authorId: number;
    genreIds: number[];
}

export interface ApiError {
    status: number;
    message?: string;
    errors?: Record<string, string>;
}
