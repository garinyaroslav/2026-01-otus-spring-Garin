import { NavLink } from 'react-router-dom';

export function Nav() {

    function handleLogout() {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        document.body.appendChild(form);
        form.submit();
    }

    return (
        <nav>
            <NavLink to="/books" className="brand">
                Library
            </NavLink>
            <NavLink to="/books" className={({ isActive }) => (isActive ? 'active' : '')}>
                Books
            </NavLink>
            <NavLink to="/authors" className={({ isActive }) => (isActive ? 'active' : '')}>
                Authors
            </NavLink>
            <NavLink to="/genres" className={({ isActive }) => (isActive ? 'active' : '')}>
                Genres
            </NavLink>
            <button onClick={handleLogout} className="btn btn-secondary">Logout</button>
        </nav>
    );
}
