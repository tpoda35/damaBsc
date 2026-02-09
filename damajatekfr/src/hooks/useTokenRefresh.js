import {useEffect} from "react";
import tokenRefreshService from "../services/TokenRefreshService.js";

const useTokenRefresh = (isAuthenticated) => {
    useEffect(() => {
        if (isAuthenticated) {
            tokenRefreshService.start();
            console.log('[useTokenRefresh] Service started');

            return () => {
                tokenRefreshService.cleanup();
                console.log('[useTokenRefresh] Service cleaned up');
            };
        }
    }, [isAuthenticated]);
};

export default useTokenRefresh;