import axios from "axios";
import { getErrorMessage } from "../Utils/getErrorMessage.js";

class ApiService {
    constructor() {
        this.client = axios.create({
            baseURL: import.meta.env.VITE_API_BASE_URL,
            headers: { "Content-Type": "application/json" },
            withCredentials: true,
        });

        this.isRefreshing = false;
        this.failedQueue = [];

        this.client.interceptors.response.use(
            (response) => response,
            (error) => this.handleError(error)
        );
    }

    processQueue(error = null) {
        this.failedQueue.forEach((prom) =>
            error ? prom.reject(error) : prom.resolve()
        );
        this.failedQueue = [];
    }

    async handleError(error) {
        const originalRequest = error.config;

        // Don't attempt refresh for auth endpoints
        if (originalRequest.url?.includes('/auth/')) {
            const message = getErrorMessage(error, "API request failed.");
            throw new Error(message);
        }

        // Stop infinite loops: if already retried refresh once, fail silently
        if (originalRequest._retryRefresh) {
            return Promise.reject(error);
        }

        // If unauthorized and not retried yet
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (this.isRefreshing) {
                // Wait until refresh completes
                return new Promise((resolve, reject) => {
                    this.failedQueue.push({ resolve, reject });
                })
                    .then(() => this.client(originalRequest))
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            this.isRefreshing = true;

            try {
                // Attempt refresh
                await this.client.post("/auth/refresh");

                this.processQueue(null);
                return this.client(originalRequest); // retry original request
            } catch (refreshError) {
                this.processQueue(refreshError);

                // Mark that refresh failed to prevent further retries
                originalRequest._retryRefresh = true;

                console.warn("Token refresh failed.");
                return Promise.reject(refreshError);
            } finally {
                this.isRefreshing = false;
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