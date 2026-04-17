import React from 'react';

export function Spinner() {
    return <div className="spinner" aria-label="Loading…" />;
}

export function ErrorMessage({ message }: { message: string }) {
    return <div className="error-message">⚠ {message}</div>;
}

export function Tag({ label }: { label: string }) {
    return <span className="tag">{label}</span>;
}

interface ConfirmProps {
    message: React.ReactNode;
    onConfirm: () => void;
    onCancel: () => void;
    busy?: boolean;
}

export function ConfirmDialog({ message, onConfirm, onCancel, busy }: ConfirmProps) {
    return (
        <div className="confirm-box">
            <p>{message}</p>
            <div className="btn-group" style={{ justifyContent: 'center' }}>
                <button className="btn btn-danger" onClick={onConfirm} disabled={busy}>
                    {busy ? 'Deleting…' : 'Yes, Delete'}
                </button>
                <button className="btn" onClick={onCancel} disabled={busy}>
                    Cancel
                </button>
            </div>
        </div>
    );
}
