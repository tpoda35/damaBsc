import { createContext, useContext, useState, useEffect, useCallback } from "react";
import ApiService from "../services/ApiService.js";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

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

    useEffect(() => {
        fetchUser();
    }, [fetchUser]);

    const login = useCallback(
        async (formData) => {
            await ApiService.post("/auth/login", formData);
            await fetchUser();
        },
        [fetchUser]
    );

    const register = useCallback(async (formData) => {
        await ApiService.post("/auth/register", formData);
    }, []);

    const logout = useCallback(async () => {
        await ApiService.post("/auth/logout");
        setUser(null);
    }, []);

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, fetchUser }}>
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
