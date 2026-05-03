import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { authorsApi, booksApi, genresApi } from '../api';
import type { BookCreateDto, BookUpdateDto } from '../types';
import { Spinner, ErrorMessage } from '../components/Common';

interface FormState {
    id: number;
    title: string;
    authorId: string;
    genreIds: number[];
}

interface Props {
    mode: 'create' | 'edit';
}

export function BookFormPage({ mode }: Props) {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [authors, setAuthors] = useState<{ id: number; fullName: string }[]>([]);
    const [genres, setGenres] = useState<{ id: number; name: string }[]>([]);
    const [form, setForm] = useState<FormState>({ id: 0, title: '', authorId: '', genreIds: [] });
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [loadError, setLoadError] = useState<string | null>(null);

    useEffect(() => {
        const promises: Promise<any>[] = [authorsApi.getAll(), genresApi.getAll()];
        if (mode === 'edit' && id) {
            promises.push(booksApi.getById(Number(id)));
        }

        Promise.all(promises)
            .then(([authorList, genreList, book]) => {
                setAuthors(authorList);
                setGenres(genreList);
                if (book) {
                    setForm({
                        id: book.id,
                        title: book.title,
                        authorId: String(book.author.id),
                        genreIds: book.genres.map((g: any) => g.id),
                    });
                }
                setLoading(false);
            })
            .catch((e: Error) => {
                setLoadError(e.message);
                setLoading(false);
            });
    }, [mode, id]);

    const toggleGenre = (gid: number) => {
        setForm(f => ({
            ...f,
            genreIds: f.genreIds.includes(gid)
                ? f.genreIds.filter(x => x !== gid)
                : [...f.genreIds, gid],
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const newErrors: Record<string, string> = {};
        if (!form.title.trim()) newErrors.title = 'Title is required';
        if (form.title.length > 150) newErrors.title = 'Title must not exceed 150 characters';
        if (!form.authorId) newErrors.authorId = 'Author is required';
        if (form.genreIds.length === 0) newErrors.genreIds = 'At least one genre must be selected';
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setSubmitting(true);
        setErrors({});

        try {
            if (mode === 'create') {
                const dto: BookCreateDto = {
                    title: form.title.trim(),
                    authorId: Number(form.authorId),
                    genreIds: form.genreIds,
                };
                await booksApi.create(dto);
            } else {
                const dto: BookUpdateDto = {
                    id: 1,
                    title: form.title.trim(),
                    authorId: Number(form.authorId),
                    genreIds: form.genreIds,
                };
                await booksApi.update(Number(id), dto);
            }
            navigate('/books');
        } catch (err: any) {
            if (err.errors) {
                setErrors(err.errors);
            } else {
                alert(err.message);
            }
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <Spinner />;
    if (loadError) return <ErrorMessage message={loadError} />;

    return (
        <main>
            <div className="page-header">
                <h1>{mode === 'create' ? 'New Book' : 'Edit Book'}</h1>
                <Link to="/books" className="btn">← All Books</Link>
            </div>

            <div className="form-card">
                {Object.keys(errors).length > 0 && (
                    <div style={{ marginBottom: '1rem', color: '#900' }}>
                        <strong>Please correct the following errors:</strong>
                        <ul>
                            {Object.values(errors).map((msg, i) => <li key={i}>{msg}</li>)}
                        </ul>
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="title">Title</label>
                        <input
                            id="title"
                            type="text"
                            className="form-control"
                            value={form.title}
                            onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                            placeholder="Enter book title"
                        />
                        {errors.title && <div style={{ color: '#900', fontSize: '0.85rem' }}>{errors.title}</div>}
                        <small>Maximum 150 characters</small>
                    </div>

                    <div className="form-group">
                        <label htmlFor="authorId">Author</label>
                        <select
                            id="authorId"
                            className="form-control"
                            value={form.authorId}
                            onChange={e => setForm(f => ({ ...f, authorId: e.target.value }))}
                        >
                            <option value="">— Select author —</option>
                            {authors.map(a => (
                                <option key={a.id} value={a.id}>{a.fullName}</option>
                            ))}
                        </select>
                        {errors.authorId && <div style={{ color: '#900', fontSize: '0.85rem' }}>{errors.authorId}</div>}
                    </div>

                    <div className="form-group">
                        <label>
                            Genres{' '}
                            <span style={{ fontWeight: 300 }}>(click to toggle)</span>
                        </label>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: '0.3rem' }}>
                            {genres.map(g => (
                                <button
                                    key={g.id}
                                    type="button"
                                    className={`btn btn-sm ${form.genreIds.includes(g.id) ? 'btn-primary' : ''}`}
                                    onClick={() => toggleGenre(g.id)}
                                >
                                    {g.name}
                                </button>
                            ))}
                        </div>
                        {errors.genreIds && <div style={{ color: '#900', fontSize: '0.85rem', marginTop: '0.3rem' }}>{errors.genreIds}</div>}
                    </div>

                    <div className="form-actions">
                        <button type="submit" className="btn btn-primary" disabled={submitting}>
                            {submitting ? 'Saving…' : mode === 'create' ? 'Create Book' : 'Save Changes'}
                        </button>
                        <Link to="/books" className="btn">Cancel</Link>
                    </div>
                </form>
            </div>
        </main>
    );
}
