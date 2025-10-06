import { createContext, useContext, useState, useEffect, useCallback } from "react";
import ApiService from "../services/ApiService.js";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const data = await ApiService.get("/auth/me");
                setUser(data);
            } catch {
                setUser(null);
            } finally {
                setLoading(false);
            }
        };

        checkAuth();
    }, []);

    const login = useCallback(async (credentials) => {
        await ApiService.post("/auth/login", credentials);
        const data = await ApiService.get("/auth/me");
        setUser(data);
    }, []);

    const register = useCallback(async (formData) => {
        await ApiService.post("/auth/register", formData);
        const data = await ApiService.get("/auth/me");
        setUser(data);
    }, []);

    const logout = useCallback(async () => {
        await ApiService.post("/auth/logout"); // implement this backend route
        setUser(null);
    }, []);

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
