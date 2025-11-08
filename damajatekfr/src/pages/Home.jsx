import { useSharedAuth } from "../contexts/AuthContext";
import GameMenu from "../components/gameMenu/GameMenu.jsx";

const Home = () => {
    const { user, loading } = useSharedAuth();

    if (loading) return <p>Loading...</p>;

    return (
        <div>
            {/*{!user ? (*/}
            {/*    <h2>Login or Register to play.</h2>*/}
            {/*) : (*/}
            {/*    <div>*/}
            {/*        <h2>Welcome back, {user.displayName || "Player"}!</h2>*/}
            {/*        <GameMenu />*/}
            {/*    </div>*/}
            {/*)}*/}
            <GameMenu />
        </div>
    );
};

export default Home;