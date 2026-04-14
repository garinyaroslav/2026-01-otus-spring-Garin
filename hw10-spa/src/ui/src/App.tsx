import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Nav } from './components/Nav';
import { BookListPage } from './pages/BookListPage';
import { BookViewPage } from './pages/BookViewPage';
import { BookFormPage } from './pages/BookFormPage';
import { AuthorListPage } from './pages/AuthorListPage';
import { GenreListPage } from './pages/GenreListPage';

export default function App() {
    return (
        <BrowserRouter>
            <Nav />
            <Routes>
                <Route path="/" element={<Navigate to="/books" replace />} />
                <Route path="/books" element={<BookListPage />} />
                <Route path="/books/create" element={<BookFormPage mode="create" />} />
                <Route path="/books/:id" element={<BookViewPage />} />
                <Route path="/books/:id/edit" element={<BookFormPage mode="edit" />} />
                <Route path="/authors" element={<AuthorListPage />} />
                <Route path="/genres" element={<GenreListPage />} />
                <Route path="*" element={
                    <main>
                        <div className="error-box">
                            <h1>404</h1>
                            <p>Page not found.</p>
                            <a href="/books" className="btn btn-primary">← Back to Books</a>
                        </div>
                    </main>
                } />
            </Routes>
            <footer>Library App</footer>
        </BrowserRouter>
    );
}
