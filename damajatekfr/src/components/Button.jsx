import React from "react";

const Button = ({ onClick, children, type = "button", style = {}, disabled = false }) => {
    // const baseStyle = {
    //     padding: "8px 16px",
    //     backgroundColor: disabled ? "#ccc" : "#007bff",
    //     color: "white",
    //     border: "none",
    //     borderRadius: "4px",
    //     cursor: disabled ? "not-allowed" : "pointer",
    //     ...style, // allow overrides
    // };

    return (
        <button type={type} onClick={onClick} style={style} disabled={disabled}>
            {children}
        </button>
    );
};

export default Button;
