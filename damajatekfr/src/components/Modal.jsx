import React from "react";
import styles from "./Modal.module.css";

const Modal = ({ isOpen, onClose, title, children }) => {
    if (!isOpen) return null;

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div className={styles.overlay} onClick={handleOverlayClick}>
            <div className={styles.modal}>
                <button
                    className={styles.closeButton}
                    onClick={onClose}
                    aria-label="Close modal"
                >
                    ×
                </button>

                {title && <h2 className={styles.title}>{title}</h2>}
                <div className={styles.modalContainer}>{children}</div>
            </div>
        </div>
    );
};

export default Modal;
