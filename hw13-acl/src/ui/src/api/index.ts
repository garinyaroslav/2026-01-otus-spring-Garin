import type {
    AuthorDto,
    BookCreateDto,
    BookDto,
    BookUpdateDto,
    CommentDto,
    GenreDto,
} from '../types';

const BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
    const res = await fetch(`${BASE}${path}`, {
        headers: { 'Content-Type': 'application/json', ...options?.headers },
        ...options,
    });

    if (res.status === 401) {
        window.location.replace('/login');
        return undefined as T;
    }

    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw Object.assign(new Error(body.message ?? 'Request failed'), {
            status: res.status,
            errors: body.errors,
        });
    }

    if (res.status === 204) return undefined as T;
    return res.json() as Promise<T>;
}

export const booksApi = {
    getAll: () => request<BookDto[]>('/books'),
    getById: (id: number) => request<BookDto>(`/books/${id}`),
    create: (dto: BookCreateDto) =>
        request<BookDto>('/books', { method: 'POST', body: JSON.stringify(dto) }),
    update: (id: number, dto: BookUpdateDto) =>
        request<BookDto>(`/books/${id}`, {
            method: 'PUT',
            body: JSON.stringify(dto),
        }),
    delete: (id: number) =>
        request<void>(`/books/${id}`, { method: 'DELETE' }),
};


export const authorsApi = {
    getAll: () => request<AuthorDto[]>('/authors'),
};

export const genresApi = {
    getAll: () => request<GenreDto[]>('/genres'),
};

export const commentsApi = {
    add: (bookId: number, text: string) => {
        const params = new URLSearchParams({ bookId: String(bookId), text });
        return request<CommentDto>(`/comments?${params}`, { method: 'POST' });
    },
    delete: (id: number) =>
        request<void>(`/comments/${id}`, { method: 'DELETE' }),
};
