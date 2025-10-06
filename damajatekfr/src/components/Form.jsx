import { useState } from "react";

const Form = ({ fields, onSubmit, buttonText, error }) => {
    const initialState = fields.reduce((acc, field) => {
        acc[field.name] = field.value || "";
        return acc;
    }, {});

    const [formData, setFormData] = useState(initialState);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit}>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {fields.map((field) => (
                <input
                    key={field.name}
                    type={field.type}
                    name={field.name}
                    placeholder={field.placeholder}
                    value={formData[field.name]}
                    onChange={handleChange}
                    required={field.required}
                />
            ))}
            <button type="submit">{buttonText}</button>
        </form>
    );
};

export default Form;
