import { genresApi } from '../api';
import { useApi } from '../hooks/useApi';
import { Spinner, ErrorMessage } from '../components/Common';

export function GenreListPage() {
    const { data: genres, loading, error } = useApi(() => genresApi.getAll(), []);

    if (loading) return <Spinner />;
    if (error) return <ErrorMessage message={error} />;

    return (
        <main>
            <div className="page-header">
                <h1>Genres</h1>
            </div>

            {genres && genres.length > 0 ? (
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        {genres.map(g => (
                            <tr key={g.id}>
                                <td>{g.id}</td>
                                <td>{g.name}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            ) : (
                <p style={{ marginTop: '2rem' }}>No genres found.</p>
            )}
        </main>
    );
}
