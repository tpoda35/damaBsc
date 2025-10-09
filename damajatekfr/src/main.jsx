import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {AuthProvider} from "./contexts/AuthContext.jsx";
import {WebSocketProvider} from "./contexts/WebSocketContext.jsx";

createRoot(document.getElementById('root')).render(
    // <StrictMode>
    <AuthProvider>
        <WebSocketProvider>
            <App />
        </WebSocketProvider>
    </AuthProvider>
    // </StrictMode>,
)
