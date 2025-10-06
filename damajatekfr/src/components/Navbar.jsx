import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Navbar = () => {
    const { user, logout } = useAuth();

    return (
        <nav style={{
            display: "flex",
            gap: "1rem",
            padding: "1rem",
            background: "#f5f5f5",
            justifyContent: "center"
        }}>
            <Link to="/">Home</Link>
            {!user ? (
                <>
                    <Link to="/login">Login</Link>
                    <Link to="/register">Register</Link>
                </>
            ) : (
                <button onClick={logout}>Logout</button>
            )}
        </nav>
    );
};

export default Navbar;