import { NavLink } from "react-router-dom";
import { useSharedAuth } from "../contexts/AuthContext";
import styles from "./Navbar.module.css";

const Navbar = () => {
    const { user, logout } = useSharedAuth();

    const linkClass = ({ isActive }) =>
        isActive ? `${styles.active}` : undefined;

    return (
        <nav className={styles.navbar}>
            <NavLink to="/" className={linkClass}>
                Home
            </NavLink>

            {!user ? (
                <>
                    <NavLink to="/login" className={linkClass}>
                        Login
                    </NavLink>
                    <NavLink to="/register" className={linkClass}>
                        Register
                    </NavLink>
                </>
            ) : (
                <>
                    <NavLink to="/profile" className={linkClass}>
                        Profile
                    </NavLink>
                    <NavLink to="/rooms" className={linkClass}>
                        Rooms
                    </NavLink>
                    <NavLink to="/friends" className={linkClass}>
                        Friends
                    </NavLink>
                    <button onClick={logout}>Logout</button>
                </>
            )}
        </nav>
    );
};

export default Navbar;
