import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import styles from "./Layout.module.css"

const Layout = () => {
    return (
        <>
            <Navbar />
            <main className={styles.main}>
                <Outlet />
            </main>
        </>
    );
}

export default Layout;
