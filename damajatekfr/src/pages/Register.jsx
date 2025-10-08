import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext";
import Form from "../components/Form.jsx";

const Register = () => {
    const { register } = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleRegister = async (data) => {
        setError("");
        try {
            await register(data);
            navigate("/");
        } catch (err) {
            // console.log("Register error: ", err);
            // console.log("Register errorMsg: ", err.message);
            setError(err.message);
        }
    };

    const fields = [
        { label: "Display name", name: "displayName", type: "text", placeholder: "MyName123", required: true },
        { label: "E-Mail", name: "email", type: "email", placeholder: "MyEmail123@mail.com", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
        { label: "Confirm password", name: "confirmPassword", type: "password", placeholder: "********", required: true },
    ];

    return <Form fields={fields} onSubmit={handleRegister} buttonText="Register" error={error} />;
};

export default Register;
