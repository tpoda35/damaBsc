import { useAuth } from "../context/AuthContext";

const Home = () => {
    const { user, loading } = useAuth();

    if (loading) return <p>Loading...</p>;

    return (
        <div className="text-center mt-10">
            {!user ? (
                <h2 className="text-xl font-semibold">
                    Login or Register to play.
                </h2>
            ) : (
                <h2 className="text-xl font-semibold">
                    Welcome back, {user.displayName || "Player"}!
                </h2>
            )}
        </div>
    );
};

export default Home;