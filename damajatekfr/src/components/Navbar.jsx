import { Link } from "react-router-dom";
import { useSharedAuth } from "../contexts/AuthContext";
import styles from "./Navbar.module.css";

const Navbar = () => {
    const { user, logout } = useSharedAuth();

    return (
        <nav className={styles.navbar}>
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
