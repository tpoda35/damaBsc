import { useSharedAuth } from "../contexts/AuthContext";
import GameMenu from "../components/gameMenu/GameMenu.jsx";
import styles from "./Home.module.css";
import Button from "../components/Button.jsx";
import { useNavigate } from "react-router-dom";

const Home = () => {
    const { user, loading } = useSharedAuth();
    const navigate = useNavigate();

    const handleLoginRedirect = () => {
        navigate("/login");
    };

    const handleRegisterRedirect = () => {
        navigate("/register");
    };

    if (loading) return <p>Loading...</p>;

    return (
        <div className={styles.home}>
            {!user ? (
                <div className={styles.hero}>
                    <h2>Login or Register to play</h2>
                    <div className={styles.buttons}>
                        <Button onClick={handleLoginRedirect} variant="primary">
                            Login
                        </Button>
                        <Button onClick={handleRegisterRedirect} variant="primary">
                            Register
                        </Button>
                    </div>
                </div>
            ) : (
                <div className={styles.dashboard}>
                    <h2>Welcome back, {user.displayName || "Player"}!</h2>
                    <GameMenu />
                </div>
            )}
        </div>
    );
};

export default Home;
