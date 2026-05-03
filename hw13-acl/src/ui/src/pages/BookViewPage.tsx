import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { booksApi, commentsApi } from '../api';
import { useApi } from '../hooks/useApi';
import { Spinner, ErrorMessage, Tag } from '../components/Common';

export function BookViewPage() {
    const { id } = useParams<{ id: string }>();
    const bookId = Number(id);
    const navigate = useNavigate();

    const { data: book, loading, error, refetch } = useApi(
        () => booksApi.getById(bookId),
        [bookId],
    );

    const [commentText, setCommentText] = useState('');
    const [posting, setPosting] = useState(false);

    const handleAddComment = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!commentText.trim()) return;
        setPosting(true);
        try {
            await commentsApi.add(bookId, commentText.trim());
            setCommentText('');
            refetch();
        } catch (err: any) {
            alert(err.message);
        } finally {
            setPosting(false);
        }
    };

    const handleDeleteComment = async (commentId: number) => {
        if (!confirm('Remove this comment?')) return;
        try {
            await commentsApi.delete(commentId);
            refetch();
        } catch (err: any) {
            alert(err.message);
        }
    };

    const handleDeleteBook = async () => {
        if (!confirm(`Delete "${book?.title}" and all its comments?`)) return;
        try {
            await booksApi.delete(bookId);
            navigate('/books');
        } catch (err: any) {
            alert(err.message);
        }
    };

    if (loading) return <Spinner />;
    if (error) return <ErrorMessage message={error} />;
    if (!book) return null;

    return (
        <main>
            <div className="page-header">
                <h1>{book.title}</h1>
                <div className="btn-group">
                    <Link to={`/books/${book.id}/edit`} className="btn">Edit</Link>
                    <button className="btn btn-danger" onClick={handleDeleteBook}>Delete</button>
                    <Link to="/books" className="btn">← All Books</Link>
                </div>
            </div>

            <div className="detail-card">
                <dl>
                    <dt>Author</dt>
                    <dd>{book.author.fullName}</dd>
                    <dt>Genres</dt>
                    <dd>{book.genres.map(g => <Tag key={g.id} label={g.name} />)}</dd>
                </dl>
            </div>

            <div className="page-header" style={{ marginTop: '2rem' }}>
                <h2>Comments</h2>
            </div>

            {book.comments.length > 0 ? (
                <ul className="comment-list">
                    {book.comments.map(c => (
                        <li key={c.id}>
                            <span>{c.text}</span>
                            <button
                                className="btn btn-danger btn-sm"
                                onClick={() => handleDeleteComment(c.id)}
                            >
                                Remove
                            </button>
                        </li>
                    ))}
                </ul>
            ) : (
                <p style={{ marginBottom: '1.5rem', fontStyle: 'italic', color: '#888' }}>
                    No comments yet.
                </p>
            )}

            <div className="comment-add">
                <form onSubmit={handleAddComment}>
                    <div className="inline-form">
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Add a comment…"
                            value={commentText}
                            onChange={e => setCommentText(e.target.value)}
                            required
                        />
                        <button type="submit" className="btn btn-primary" disabled={posting}>
                            {posting ? '…' : 'Post'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}
