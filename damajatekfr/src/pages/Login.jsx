import { useSharedAuth } from "../contexts/AuthContext";
import Form from "../components/Form.jsx";
import { useState } from "react";
import "./Login.module.css";
import {useNavigate} from "react-router-dom";


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
            setError(err.message);
        }
    };


    const fields = [
        {
            label: "E-Mail",
            name: "email",
            type: "email",
            placeholder: "MyEmail123@mail.com",
            required: true,
        },
        {
            label: "Password",
            name: "password",
            type: "password",
            placeholder: "********",
            required: true,
        },
    ];


    return (
        <div className="login-container">
            <div className="login-card">
                <h2 className="login-title">Welcome Back</h2>
                <p className="login-subtitle">Please log in to continue</p>
                <Form
                    fields={fields}
                    onSubmit={handleLogin}
                    buttonText="Login"
                    error={error}
                />
            </div>
        </div>
    );
};


export default Login;