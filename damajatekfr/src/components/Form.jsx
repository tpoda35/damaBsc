import { useState } from "react";

import "./Form.module.css"

// Example field config for reference:
// { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
// { label: "Favorite Color", name: "color", type: "select", options: [{ label: "Red", value: "red" }], required: true }

const Form = ({ fields, onSubmit, buttonText, error, conditionalRender }) => {
    // It creates the formData var "build"
    const initialState = fields.reduce((acc, field) => {
        if (field.type === "checkbox") {
            acc[field.name] = field.value || false;
        } else {
            acc[field.name] = field.value || "";
        }
        return acc;
    }, {});

    const [formData, setFormData] = useState(initialState);

    const handleChange = (e) => {
        const { name, type, value, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === "checkbox" ? checked : value,
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // If locked is true, password must be provided
        if (formData.locked && !formData.password) {
            alert("Password is required when the room is locked!");
            return;
        }

        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit}>
            {error && <p style={{ color: "red" }}>{error}</p>}

            {fields.map((field) => {
                if (conditionalRender && !conditionalRender(formData, field)) return null;

                return (
                    <div key={field.name} style={{ marginBottom: "1rem" }}>
                        <label
                            htmlFor={field.name}
                            style={{ display: "block", marginBottom: "0.25rem" }}
                        >
                            {field.label || field.name}
                        </label>

                        {/* Select fields */}
                        {field.type === "select" ? (
                            <select
                                id={field.name}
                                name={field.name}
                                value={formData[field.name]}
                                onChange={handleChange}
                                required={field.required}
                            >
                                <option value="">Select an option</option>
                                {field.options?.map((option) => (
                                    <option key={option.value} value={option.value}>
                                        {option.label}
                                    </option>
                                ))}
                            </select>
                        ) : (
                            <input
                                id={field.name}
                                type={field.type}
                                name={field.name}
                                placeholder={field.placeholder}
                                value={
                                    field.type !== "checkbox" ? formData[field.name] : undefined
                                }
                                checked={
                                    field.type === "checkbox" ? formData[field.name] : undefined
                                }
                                onChange={handleChange}
                                required={field.type === "checkbox" ? false : field.required}
                            />
                        )}
                    </div>
                );
            })}

            <button type="submit">{buttonText}</button>
        </form>
    );
};

export default Form;
