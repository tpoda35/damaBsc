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
import VsPlayer from "./components/gameMenu/VsPlayer.jsx";

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
        <BrowserRouter>
            <Routes>
                <Route element={<Layout />}>
                    <Route path="/" element={<Home />} />
                    <Route path="/vsPlayer" element={<VsPlayer />} />
                    <Route path="/rooms" element={<Rooms />} />
                    <Route path="/rooms/:roomId" element={<Room />} />
                    <Route path="/games/:gameId" element={<Game />} />
                </Route>

                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
