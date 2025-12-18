import React from "react";
import styles from "./Button.module.css";

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

