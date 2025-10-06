import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";

const Layout = () => {
    return (
        <div className="app-layout">
            <Navbar />
            <main className="p-4">
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;
