import {useEffect} from "react";
import tokenRefreshService from "../services/TokenRefreshService.js";

const useTokenRefresh = (isAuthenticated) => {
    useEffect(() => {
        if (isAuthenticated) {
            // Start the token refresh timer when user is authenticated
            tokenRefreshService.start();
            console.log('[useTokenRefresh] Service started');

            return () => {
                // Cleanup when unmounting or user logs out
                tokenRefreshService.cleanup();
                console.log('[useTokenRefresh] Service cleaned up');
            };
        }
    }, [isAuthenticated]);
};

export default useTokenRefresh;