import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {AuthProvider} from "./contexts/AuthContext.jsx";
import {WebSocketProvider} from "./contexts/WebSocketContext.jsx";
import {BrowserRouter} from "react-router-dom";

createRoot(document.getElementById('root')).render(
    // <StrictMode>
    <AuthProvider>
        <WebSocketProvider>
            <BrowserRouter>
                <App />
            </BrowserRouter>
        </WebSocketProvider>
    </AuthProvider>
    // </StrictMode>,
)
