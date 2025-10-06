import axios from "axios";
import { getErrorMessage } from "../Utils/getErrorMessage.js";

class ApiService {
    constructor() {
        this.client = axios.create({
            baseURL: import.meta.env.VITE_API_BASE_URL,
            headers: { "Content-Type": "application/json" },
            withCredentials: true,
        });
    }

    async request(config) {
        try {
            const response = await this.client.request(config);
            return response.data;
        } catch (err) {
            console.error("API Request failed:", err);
            const message = getErrorMessage(err, "API request failed.");
            console.log('custom message: ', message);
            throw new Error(message);
        }
    }

    get(endpoint) { return this.request({ method: "GET", url: endpoint }); }
    post(endpoint, data) { return this.request({ method: "POST", url: endpoint, data }); }
    patch(endpoint, data) { return this.request({ method: "PATCH", url: endpoint, data }); }
    delete(endpoint) { return this.request({ method: "DELETE", url: endpoint }); }
}

export default new ApiService();
