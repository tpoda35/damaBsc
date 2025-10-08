import React from "react";

const Modal = ({ isOpen, onClose, title, children }) => {
    if (!isOpen) return null; // Don't render if not open

    const handleOverlayClick = (e) => {
        // Close modal when clicking outside the content
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div
            onClick={handleOverlayClick}
            style={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "100vw",
                height: "100vh",
                backgroundColor: "rgba(0,0,0,0.5)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                zIndex: 1000,
            }}
        >
            <div
                style={{
                    backgroundColor: "white",
                    padding: "1rem",
                    minWidth: "300px",
                    maxWidth: "90%",
                    maxHeight: "90%",
                    overflowY: "auto",
                    borderRadius: "4px",
                    color: "black"
                }}
            >
                {title && <h2>{title}</h2>}
                <div>{children}</div>
                <button onClick={onClose} style={{ marginTop: "1rem" }}>
                    Close
                </button>
            </div>
        </div>
    );
};

export default Modal;
