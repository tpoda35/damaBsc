import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {useSharedAuth} from "../contexts/AuthContext";
import Form from "../components/Form.jsx";
import './Auth.css';
import {toast} from "react-toastify";
import {getErrorMessage} from "../utils/getErrorMessage.js";
import {Link} from "react-router";

const Register = () => {
    const { register } = useSharedAuth();
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleRegister = async (data) => {
        setError("");
        try {
            await register(data);
            toast.success("Registration successful");
            navigate("/");
        } catch (err) {
            setError(getErrorMessage(err, "Registration failed"));
        }
    };

    const fields = [
        { label: "Display name", name: "displayName", type: "text", placeholder: "MyName123", required: true },
        { label: "E-Mail", name: "email", type: "email", placeholder: "MyEmail123@mail.com", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
        { label: "Confirm password", name: "confirmPassword", type: "password", placeholder: "********", required: true }
    ];

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">Welcome!</h2>
                <p className="auth-subtitle">Please register to continue</p>
                <Form
                    fields={fields}
                    onSubmit={handleRegister}
                    buttonText="Register"
                    error={error}
                />

                <p className="auth-footer">
                    Already have an account?{" "}
                    <Link to="/login" className="auth-link">
                        Login here
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default Register;
