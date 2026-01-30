import {motion} from "framer-motion";
import styles from "./Loader.module.css";

const Loader = ({ text = "Loading..." }) => {
    return (
        <motion.div
            className={styles.loaderWrapper}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
        >
            <div className={styles.spinner}></div>
            <p className={styles.text}>{text}</p>
        </motion.div>
    );
};

export default Loader;
