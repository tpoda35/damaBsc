import {BrowserRouter, Route, Routes} from "react-router-dom";
import Layout from "./components/Layout";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Rooms from "./pages/Rooms.jsx";
import Room from "./pages/Room.jsx";
import {useSharedAuth} from "./contexts/AuthContext.jsx";
import {useSharedWebSocket} from "./contexts/WebSocketContext.jsx";
import useTokenRefresh from "./hooks/useTokenRefresh.js";
import {useEffect} from "react";
import Game from "./pages/Game.jsx";
import Profile from "./pages/Profile.jsx";
import {Bounce, ToastContainer} from "react-toastify";
import GameEnded from "./pages/GameEnded.jsx";
import "./Toast.css";

function App() {
    const { user } = useSharedAuth();
    const { connect, disconnect, isConnected, subscribe } = useSharedWebSocket();

    const isAuthenticated = !!user;

    useTokenRefresh(isAuthenticated);

    useEffect(() => {
        if (isAuthenticated && !isConnected) {
            connect();
        } else if (!isAuthenticated && isConnected) {
            disconnect();
        }
    }, [isAuthenticated, isConnected, connect, disconnect]);

    useEffect(() => {
        if (!isAuthenticated || !isConnected) return;

        const unsubscribeErrors = subscribe('/user/queue/errors', (message) => {
            const errorMsg = JSON.parse(message.body).message;

            alert(`Error: ${errorMsg}`);
        });

        return () => {
            if (unsubscribeErrors) unsubscribeErrors();
        };
    }, [isAuthenticated, isConnected, subscribe]);


    return (
        <>
            <BrowserRouter>
                <ToastContainer
                    className="appToastContainer"
                    toastClassName="appToast"
                    bodyClassName="appToastBody"
                    position="top-left"
                    autoClose={5000}
                    hideProgressBar={false}
                    closeOnClick={false}
                    pauseOnFocusLoss
                    draggable
                    pauseOnHover
                    theme="light"
                    transition={Bounce}
                />

                <Routes>
                    <Route element={<Layout />}>
                        <Route path="/" element={<Home />} />
                        <Route path="/profile" element={<Profile />} />
                        <Route path="/rooms" element={<Rooms />} />
                    </Route>

                    <Route path="/rooms/:roomId" element={<Room />} />

                    <Route path="/games/:gameId" element={<Game />} />
                    <Route path="/game-ended" element={<GameEnded />} />

                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                </Routes>
            </BrowserRouter>
        </>
    );
}

export default App;
