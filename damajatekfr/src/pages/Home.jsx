import { useSharedAuth } from "../contexts/AuthContext";

const Home = () => {
    const { user, loading } = useSharedAuth();

    if (loading) return <p>Loading...</p>;

    return (
        <div>
            {!user ? (
                <h2>
                    Login or Register to play.
                </h2>
            ) : (
                <h2>
                    Welcome back, {user.displayName || "Player"}!
                </h2>
            )}
        </div>
    );
};

export default Home;