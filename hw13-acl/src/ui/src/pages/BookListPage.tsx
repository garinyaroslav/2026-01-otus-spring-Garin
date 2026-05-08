import { useState } from 'react';
import { Link } from 'react-router-dom';
import { booksApi } from '../api';
import { useApi } from '../hooks/useApi';
import { Spinner, ErrorMessage, Tag } from '../components/Common';

export function BookListPage() {
    const { data: books, loading, error, refetch } = useApi(() => booksApi.getAll(), []);
    const [deleting, setDeleting] = useState<number | null>(null);

    const handleDelete = async (id: number) => {
        if (!confirm('Delete this book and all its comments?')) return;
        setDeleting(id);
        try {
            await booksApi.delete(id);
            refetch();
        } catch (e: any) {
            alert(e.message);
        } finally {
            setDeleting(null);
        }
    };

    if (loading) return <Spinner />;
    if (error) return <ErrorMessage message={error} />;

    return (
        <main>
            <div className="page-header">
                <h1>Books</h1>
                <Link to="/books/create" className="btn btn-primary">+ Add Book</Link>
            </div>

            {books && books.length > 0 ? (
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Title</th>
                            <th>Author</th>
                            <th>Genres</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {books.map(book => (
                            <tr key={book.id}>
                                <td>{book.id}</td>
                                <td>
                                    <Link to={`/books/${book.id}`} style={{ color: 'inherit', fontWeight: 600 }}>
                                        {book.title}
                                    </Link>
                                </td>
                                <td>{book.author.fullName}</td>
                                <td>{book.genres.map(g => <Tag key={g.id} label={g.name} />)}</td>
                                <td>
                                    <div className="btn-group">
                                        <Link to={`/books/${book.id}`} className="btn btn-sm">View</Link>
                                        <Link to={`/books/${book.id}/edit`} className="btn btn-sm">Edit</Link>
                                        <button
                                            className="btn btn-danger btn-sm"
                                            onClick={() => handleDelete(book.id)}
                                            disabled={deleting === book.id}
                                        >
                                            {deleting === book.id ? '…' : 'Delete'}
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            ) : (
                <p style={{ marginTop: '2rem' }}>
                    No books yet. <Link to="/books/create">Add the first one.</Link>
                </p>
            )}
        </main>
    );
}
