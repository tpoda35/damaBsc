import { useState } from "react";

// { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
// Important:
// name - this will be in the response
// type - field type
const Form = ({ fields, onSubmit, buttonText, error, conditionalRender }) => {
    // It creates the formData
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
        // If there's no locked or password, the check will simply skip
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
                        <label htmlFor={field.name} style={{ display: "block", marginBottom: "0.25rem" }}>
                            {field.label || field.name}
                        </label>
                        <input
                            id={field.name}
                            type={field.type}
                            name={field.name}
                            placeholder={field.placeholder}
                            value={field.type !== "checkbox" ? formData[field.name] : undefined}
                            checked={field.type === "checkbox" ? formData[field.name] : undefined}
                            onChange={handleChange}
                            required={field.type === "checkbox" ? false : field.required}
                        />
                    </div>
                );
            })}

            <button type="submit">{buttonText}</button>
        </form>
    );
};

export default Form;
