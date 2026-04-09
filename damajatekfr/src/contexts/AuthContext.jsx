import { createContext, useContext, useState, useEffect, useCallback } from "react";
import ApiService from "../services/ApiService.js";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(false);
    const [inGame, setInGame] = useState(false);

    const fetchUser = useCallback(async () => {
        setLoading(true);
        try {
            const data = await ApiService.get("/users");
            setUser(data);
        } catch {
            setUser(null);
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchInGameStatus = useCallback(async () => {
        try {
            const data = await ApiService.get("/games/in-progress");

            if (data.isInGame) {
                setInGame({
                    isInGame: true,
                    gameId: data.gameId
                });
            } else {
                setInGame({ isInGame: false });
            }
        } catch {
            setInGame({ isInGame: false });
        }
    }, []);

    useEffect(() => {
        fetchUser();
    }, [fetchUser]);

    useEffect(() => {
        if (user) {
            fetchInGameStatus();
        } else {
            setInGame(null);
        }
    }, [user, fetchInGameStatus]);

    const login = useCallback(async (formData) => {
        setLoading(true);
        try {
            await ApiService.post("/auth/login", formData);
            await fetchUser();
        } finally {
            setLoading(false);
        }
    }, [fetchUser]);
    
    const register = useCallback(async (formData) => {
        setLoading(true);
        try {
            await ApiService.post("/auth/register", formData);
        } finally {
            setLoading(false);
        }
    }, []);

    const logout = useCallback(async () => {
        setLoading(true);
        try {
            await ApiService.post("/auth/logout");
            setUser(null);
        } finally {
            setLoading(false);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, fetchUser, inGame }}>
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export function useSharedAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useSharedAuth must be used within a AuthProvider');
    }
    return context;
}
