import { motion } from "framer-motion";

const AnimatedPage = ({ children, className }) => {
    return (
        <motion.div
            className={className}
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1, ease: "easeOut" }}
            style={{ willChange: "transform, opacity" }}
        >
            {children}
        </motion.div>
    );
};

export default AnimatedPage;
