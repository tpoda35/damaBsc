import axios from "axios";
import { tokenRefreshEmitter } from "./TokenRefreshEmitter.js";

class TokenRefreshService {
    constructor() {
        this.client = axios.create({
            baseURL: import.meta.env.VITE_API_BASE_URL,
            headers: { "Content-Type": "application/json" },
            withCredentials: true,
        });

        this.isRefreshing = false;
        this.failedQueue = [];

        this.refreshTimerRef = null;
        // Token TTL is 30 minutes, refresh at 25 minutes
        this.TOKEN_REFRESH_INTERVAL = 25 * 60 * 1000;
    }


    // Start the refresh timer
    start() {
        // Clear any existing timer
        if (this.refreshTimerRef) {
            clearInterval(this.refreshTimerRef);
        }

        // Set up interval to refresh token
        this.refreshTimerRef = setInterval(async () => {
            console.log('[TokenRefreshService] Token refresh triggered');
            try {
                await this.refreshToken();
            } catch (err) {
                console.warn('[TokenRefreshService] Refresh failed:', err.message);
            }
        }, this.TOKEN_REFRESH_INTERVAL);

        console.log(`[TokenRefreshService] Started (refreshing every ${this.TOKEN_REFRESH_INTERVAL / 60000} minutes)`);
    }

    // Stop the refresh timer
    stop() {
        if (this.refreshTimerRef) {
            clearInterval(this.refreshTimerRef);
            this.refreshTimerRef = null;
            console.log('[TokenRefreshService] Stopped');
        }
    }

    // Process queued requests after refresh completes
    processQueue(error = null) {
        this.failedQueue.forEach((prom) =>
            error ? prom.reject(error) : prom.resolve()
        );
        this.failedQueue = [];
    }

    // Refresh the access token.
    // If already refreshing, returns a promise that resolves when current refresh completes
    async refreshToken() {
        // If already refreshing, queue this request
        if (this.isRefreshing) {
            return new Promise((resolve, reject) => {
                this.failedQueue.push({ resolve, reject });
            });
        }

        this.isRefreshing = true;

        try {
            await this.client.post("/auth/refresh");

            // Notify all listeners that token was refreshed
            tokenRefreshEmitter.emit();
            console.log('[TokenRefreshService] Token refreshed successfully, notified listeners');

            // Process all queued requests
            this.processQueue(null);
        } catch (error) {
            console.error('[TokenRefreshService] Token refresh failed:', error.message);

            // Reject all queued requests
            this.processQueue(error);

            // Stop the timer since we can't refresh anymore
            this.stop();

            throw error;
        } finally {
            this.isRefreshing = false;
        }
    }

    // Manually trigger a token refresh
    async refresh() {
        return this.refreshToken();
    }

    // Cleanup resources (call on logout)
    cleanup() {
        this.stop();
        this.failedQueue = [];
    }
}

export default new TokenRefreshService();