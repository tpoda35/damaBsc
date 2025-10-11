import { createContext, useContext, useState, useEffect, useCallback } from "react";
import ApiService from "../services/ApiService.js";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(false);

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
            ApiService.cleanup();
        } finally {
            setLoading(false);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, fetchUser }}>
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
