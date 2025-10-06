import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Form from "../components/Form.jsx";

const Login = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleLogin = async (data) => {
        await login(data);
        navigate("/");
    };

    const fields = [
        { name: "email", type: "email", placeholder: "Email", required: true },
        { name: "password", type: "password", placeholder: "Password", required: true },
    ];

    return <Form fields={fields} onSubmit={handleLogin} buttonText="Login" />;
};

export default Login;
