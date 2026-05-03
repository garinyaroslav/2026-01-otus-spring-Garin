import { useState } from 'react';

export function LoginPage() {
    const [error, setError] = useState(
        new URLSearchParams(window.location.search).has('error')
    );
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(false);
        setLoading(true);

        const data = new URLSearchParams({
            username: (e.currentTarget.elements.namedItem('username') as HTMLInputElement).value,
            password: (e.currentTarget.elements.namedItem('password') as HTMLInputElement).value,
        });

        try {
            const res = await fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data.toString(),
                redirect: 'manual',
            });

            if (res.type === 'opaqueredirect' || res.ok) {
                window.location.replace('/books');
            } else {
                setError(true);
            }
        } catch {
            setError(true);
        } finally {
            setLoading(false);
        }
    }

    return (
        <main className="login-page">
            <form className="login-form" onSubmit={handleSubmit}>
                <h1>Library App</h1>
                <h2>Log in</h2>
                {error && <p className="error-msg">Invalid password and login</p>}
                <label>
                    Login
                    <input name="username" type="text" autoComplete="username" required />
                </label>
                <label>
                    Password
                    <input name="password" type="password" autoComplete="current-password" required />
                </label>
                <button type="submit" disabled={loading}>
                    {loading ? 'Logging…' : 'Log on'}
                </button>
            </form>
        </main>
    );
}
