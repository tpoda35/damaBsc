import useWebSocket from "../hooks/useWebSocket.js";
import {createContext, useContext} from "react";

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
    const ws = useWebSocket();

    return (
        <WebSocketContext.Provider value={ws}>
            {children}
        </WebSocketContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useSharedWebSocket() {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error('useSharedWebSocket must be used within a WebSocketProvider');
    }
    return context;
}