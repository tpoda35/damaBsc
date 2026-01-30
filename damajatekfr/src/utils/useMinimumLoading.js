import { useEffect, useState } from "react";

export function useMinimumLoading(isLoading, minDuration = 500) {
    const [showLoader, setShowLoader] = useState(isLoading);

    useEffect(() => {
        let timeout;

        if (isLoading) {
            setShowLoader(true);
        } else {
            timeout = setTimeout(() => {
                setShowLoader(false);
            }, minDuration);
        }

        return () => clearTimeout(timeout);
    }, [isLoading, minDuration]);

    return showLoader;
}
