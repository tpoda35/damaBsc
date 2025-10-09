import { useNavigate } from "react-router-dom";
import { useSharedAuth } from "../contexts/AuthContext";
import Form from "../components/Form.jsx";
import {useState} from "react";

const Login = () => {
    const { login } = useSharedAuth();
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleLogin = async (data) => {
        setError("");
        try {
            await login(data);
            navigate("/");
        } catch (err) {
            // console.log("Login error:", err);
            // console.log("Login errorMsg:", err.message);
            setError(err.message);
        }

    };

    const fields = [
        { label: "E-Mail", name: "email", type: "email", placeholder: "MyEmail123@mail.com", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
    ];

    return <Form fields={fields} onSubmit={handleLogin} buttonText="Login" error={error} />;
};

export default Login;
