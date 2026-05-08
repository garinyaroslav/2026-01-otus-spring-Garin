import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export function useAuth() {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (location.pathname === '/login') return;

        fetch('/api/me')
            .then(res => {
                if (res.status === 401) navigate('/login', { replace: true });
            });
    }, [location.pathname]);
}
