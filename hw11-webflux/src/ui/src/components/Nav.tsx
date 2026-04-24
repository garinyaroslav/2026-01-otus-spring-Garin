import { NavLink } from 'react-router-dom';

export function Nav() {
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
        </nav>
    );
}
