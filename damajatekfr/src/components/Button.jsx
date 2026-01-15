import React from "react";
import styles from "./Button.module.css";

/*
Variants:
    <Button variant="primary">Primary</Button>
    <Button variant="light">Light</Button>
    <Button variant="success">Success</Button>
    <Button variant="warning">Warning</Button>
    <Button variant="error">Error</Button>
    <Button variant="info">Info</Button>
    <Button variant="primary" disabled>Disabled</Button>
    <Button variant="primary" fullWidth>Full width</Button>
 */

const Button = ({
                    onClick,
                    children,
                    type = "button",
                    variant = "primary",
                    disabled = false,
                    fullWidth = false,
                }) => {
    return (
        <button
            type={type}
            onClick={onClick}
            className={`${styles.button} ${styles[variant]} ${
                fullWidth ? styles.fullWidth : ""
            }`}
            disabled={disabled}
        >
            {children}
        </button>
    );
};

export default Button;

