import { useSharedAuth } from "../contexts/AuthContext";
import Form from "../components/Form.jsx";
import { useState } from "react";
import  "./Auth.css";
import {useNavigate} from "react-router-dom";
import {toast} from "react-toastify";
import {getErrorMessage} from "../utils/getErrorMessage.js";
import {Link} from "react-router";

const Login = () => {
    const { login } = useSharedAuth();
    const navigate = useNavigate();
    const [error, setError] = useState("");


    const handleLogin = async (data) => {
        setError("");
        try {
            await login(data);
            toast.success("Login successful");
            navigate("/");
        } catch (err) {
            setError(getErrorMessage(err, "Login failed"));
        }
    };


    const fields = [
        { label: "E-Mail", name: "email", type: "email", placeholder: "MyEmail123@mail.com", required: true,},
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true,}
    ];

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">Welcome!</h2>
                <p className="auth-subtitle">Please log in to continue</p>
                <Form
                    fields={fields}
                    onSubmit={handleLogin}
                    buttonText="Login"
                    error={error}
                />

                <p className="auth-footer">
                    No account?{" "}
                    <Link to="/register" className="auth-link">
                        Register here
                    </Link>
                </p>
            </div>
        </div>
    );
};


export default Login;