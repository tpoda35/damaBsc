import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Form from "../components/Form.jsx";

const Register = () => {
    const { register } = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleRegister = async (data) => {
        setError("");
        console.log(data);
        try {
            await register(data);
            navigate("/");
        } catch (err) {
            console.error(err);
            setError("Failed to register. Try again.");
        }
    };

    const fields = [
        { name: "displayName", type: "text", placeholder: "Display name", required: true },
        { name: "email", type: "email", placeholder: "Email", required: true },
        { name: "password", type: "password", placeholder: "Password", required: true },
        { name: "confirmPassword", type: "password", placeholder: "Confirm password", required: true },
    ];

    return <Form fields={fields} onSubmit={handleRegister} buttonText="Register" error={error} />;
};

export default Register;
