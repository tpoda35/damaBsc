import axios from "axios";
import { getErrorMessage } from "../utils/getErrorMessage.js";
import tokenRefreshService from "./TokenRefreshService.js";

class ApiService {
    constructor() {
        this.client = axios.create({
            baseURL: import.meta.env.VITE_API_BASE_URL,
            headers: { "Content-Type": "application/json" },
            withCredentials: true,
        });

        this.client.interceptors.response.use(
            (response) => response,
            (error) => this.handleError(error)
        );
    }

    async handleError(error) {
        const originalRequest = error.config;

        // Don't attempt refresh for auth endpoints except ws-token
        if (originalRequest.url?.includes('/auth/') &&
            !originalRequest.url?.includes('/auth/ws-token')) {
            const message = getErrorMessage(error, "API request failed.");
            throw new Error(message);
        }

        // Stop infinite loops: if already retried refresh once, fail
        if (originalRequest._retryRefresh) {
            return Promise.reject(error);
        }

        // If unauthorized and not retried yet (FALLBACK for missed timer refreshes)
        if (error.response?.status === 401 && !originalRequest._retry) {
            console.log('[ApiService] 401 detected, attempting refresh (fallback)');

            originalRequest._retry = true;

            try {
                // Use TokenRefreshService to handle the refresh
                await tokenRefreshService.refresh();

                // Retry the original request
                return this.client(originalRequest);
            } catch (refreshError) {
                // Mark that refresh failed to prevent further retries
                originalRequest._retryRefresh = true;
                console.warn('[ApiService] Token refresh failed');

                return Promise.reject(refreshError);
            }
        }

        // Otherwise, just handle as a normal error
        const message = getErrorMessage(error, "API request failed.");
        throw new Error(message);
    }

    async request(config) {
        try {
            const response = await this.client.request(config);
            return response.data;
        } catch (err) {
            console.error(err);
            throw err;
        }
    }

    get(endpoint) {
        return this.request({ method: "GET", url: endpoint });
    }

    post(endpoint, data) {
        return this.request({ method: "POST", url: endpoint, data });
    }

    patch(endpoint, data) {
        return this.request({ method: "PATCH", url: endpoint, data });
    }

    delete(endpoint) {
        return this.request({ method: "DELETE", url: endpoint });
    }
}

export default new ApiService();