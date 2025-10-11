class TokenRefreshEmitter {
    constructor() {
        this.listeners = [];
    }

    subscribe(callback) {
        this.listeners.push(callback);
        return () => {
            this.listeners = this.listeners.filter(cb => cb !== callback);
        };
    }

    emit() {
        this.listeners.forEach(callback => {
            try {
                callback();
            } catch (err) {
                console.error('[TokenRefresh] Listener error:', err);
            }
        });
    }
}

export const tokenRefreshEmitter = new TokenRefreshEmitter();