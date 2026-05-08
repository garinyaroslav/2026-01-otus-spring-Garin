import { authorsApi } from '../api';
import { useApi } from '../hooks/useApi';
import { Spinner, ErrorMessage } from '../components/Common';

export function AuthorListPage() {
    const { data: authors, loading, error } = useApi(() => authorsApi.getAll(), []);

    if (loading) return <Spinner />;
    if (error) return <ErrorMessage message={error} />;

    return (
        <main>
            <div className="page-header">
                <h1>Authors</h1>
            </div>

            {authors && authors.length > 0 ? (
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Full Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        {authors.map(a => (
                            <tr key={a.id}>
                                <td>{a.id}</td>
                                <td>{a.fullName}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            ) : (
                <p style={{ marginTop: '2rem' }}>No authors found.</p>
            )}
        </main>
    );
}
