import { Link } from "react-router-dom";
import { useSharedAuth } from "../contexts/AuthContext";

const Navbar = () => {
    const { user, logout } = useSharedAuth();

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
                <>
                    <Link to="/profile">Profile</Link>
                    <Link to="/rooms">Rooms</Link>
                    <Link to="/friends">Friends</Link>
                    <button onClick={logout}>Logout</button>
                </>
            )}
        </nav>
    );
};

export default Navbar;