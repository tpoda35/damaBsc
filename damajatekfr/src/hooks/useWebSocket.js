import {useCallback, useEffect, useRef, useState} from 'react';
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';
import apiService from '../services/ApiService.js';
import {tokenRefreshEmitter} from "../services/TokenRefreshEmitter.js";

const websocketUrl = import.meta.env.VITE_API_WEBSOCKET_URL;

// Older messages will be dropped
const MAX_QUEUE_SIZE = 500;

const useWebSocket = () => {
    const stompClientRef = useRef(null);

    const [isConnected, setIsConnected] = useState(false);
    const [isConnecting, setIsConnecting] = useState(false);

    // Map<destination, { callback, headers, subscription }>
    const subscriptionsRef = useRef(new Map());

    // Prevent connect race conditions
    const connectAttemptIdRef = useRef(0);
    const pendingConnectPromiseRef = useRef(null);

    // Queue for messages attempted while disconnected
    const messageQueueRef = useRef([]);

    // Debounce reconnect requests
    const reconnectTimeoutRef = useRef(null);

    const flushQueue = useCallback(() => {
        if (!stompClientRef.current?.connected) return;
        const client = stompClientRef.current;
        const queue = messageQueueRef.current;
        messageQueueRef.current = [];

        const failed = [];
        queue.forEach(({ destination, body, headers, retryCount = 0 }) => {
            try {
                client.publish({ destination, body, headers });
            } catch {
                if (retryCount < 3) {
                    failed.push({ destination, body, headers, retryCount: retryCount + 1 });
                } else {
                    console.error('[WebSocket] Dropped message after max retries:', destination);
                }
            }
        });
        messageQueueRef.current = failed;
    }, []);

    const resubscribeAll = useCallback((client) => {
        subscriptionsRef.current.forEach((subData, destination) => {
            try {
                const newSub = client.subscribe(destination, subData.callback, subData.headers || {});
                subscriptionsRef.current.set(destination, { ...subData, subscription: newSub });
            } catch (e) {
                console.error(`[WebSocket] Failed to resubscribe to ${destination}`, e);
            }
        });
    }, []);

    const deactivateClient = useCallback(() => {
        const client = stompClientRef.current;
        if (!client || !client.active) return Promise.resolve();

        return new Promise((resolve) => {
            let resolved = false;
            const originalOnDisconnect = client.onDisconnect;

            client.onDisconnect = (frame) => {
                if (!resolved) {
                    resolved = true;
                    setIsConnected(false);
                    setIsConnecting(false);
                    resolve();
                }
                if (originalOnDisconnect) originalOnDisconnect(frame);
            };

            client.deactivate();

            // Safety timeout
            setTimeout(() => {
                if (!resolved) {
                    resolved = true;
                    resolve();
                }
            }, 3000);
        });
    }, []);

    const connect = useCallback(() => {
        // Return existing in-flight promise if any
        if (pendingConnectPromiseRef.current) {
            return pendingConnectPromiseRef.current;
        }

        if (stompClientRef.current?.connected) {
            return Promise.resolve(stompClientRef.current);
        }

        const attemptId = ++connectAttemptIdRef.current;

        const promise = (async () => {
            setIsConnecting(true);

            let token;
            try {
                // Get WebSocket token (ApiService will handle 401 refresh if needed)
                token = await apiService.post('/auth/ws-token');
                console.log('[WebSocket] Got WebSocket token');
            } catch (err) {
                console.error('[WebSocket] Failed to get WebSocket token', err);
                setIsConnecting(false);
                throw new Error('Cannot obtain WebSocket token');
            }

            // Deactivate old client if exists
            if (stompClientRef.current) {
                try {
                    stompClientRef.current.deactivate();
                } catch {
                    /* noop */
                }
            }

            return await new Promise((resolve, reject) => {
                let timer = null;

                const socket = new SockJS(websocketUrl, null, { withCredentials: true });
                const client = new Client({
                    webSocketFactory: () => socket,
                    debug: (msg) => console.log('[STOMP]', msg),
                    reconnectDelay: 5000,

                    heartbeatIncoming: 10000,
                    heartbeatOutgoing: 10000,

                    connectHeaders: {
                        Authorization: `Bearer ${token}`
                    },

                    onConnect: () => {
                        // Ignore stale connects
                        if (attemptId !== connectAttemptIdRef.current) return;

                        if (timer) clearTimeout(timer);
                        setIsConnected(true);
                        setIsConnecting(false);

                        try {
                            resubscribeAll(client);
                            flushQueue();
                        } catch (e) {
                            console.error('[WebSocket] Post-connect error', e);
                        }

                        console.log('[WebSocket] Connected successfully');
                        resolve(client);
                    },

                    onDisconnect: () => {
                        if (attemptId === connectAttemptIdRef.current) {
                            setIsConnected(false);
                            setIsConnecting(false);
                        }
                    },

                    onStompError: (frame) => {
                        if (timer) clearTimeout(timer);
                        console.error('[WebSocket] STOMP Error:', frame.headers?.message);
                        console.error('[WebSocket] Details:', frame.body);

                        if (attemptId === connectAttemptIdRef.current) {
                            setIsConnecting(false);
                        }
                        reject(new Error(frame.headers?.message || 'STOMP connection error'));
                    },
                });

                stompClientRef.current = client;
                client.activate();

                // Safety timeout
                timer = setTimeout(() => {
                    setIsConnecting(false);
                    reject(new Error('[WebSocket] Connection timeout'));
                }, 10000);
            });
        })()
            .finally(() => {
                if (pendingConnectPromiseRef.current === promise) {
                    pendingConnectPromiseRef.current = null;
                }
            });

        pendingConnectPromiseRef.current = promise;
        return promise;
    }, [flushQueue, resubscribeAll]);

    const disconnect = useCallback(() => {
        // Clear any pending reconnect
        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
            reconnectTimeoutRef.current = null;
        }

        // Unsubscribe from all subscriptions
        subscriptionsRef.current.forEach((sub) => {
            try {
                sub.subscription?.unsubscribe();
            } catch {
                /* noop */
            }
        });
        subscriptionsRef.current.clear();

        // Deactivate client
        try {
            stompClientRef.current?.deactivate();
        } finally {
            stompClientRef.current = null;
            setIsConnected(false);
            setIsConnecting(false);
        }
    }, []);

    const reconnect = useCallback(async () => {
        console.log('[WebSocket] Reconnecting due to token refresh...');
        try {
            await deactivateClient();
            await connect();
        } catch (e) {
            console.error('[WebSocket] Reconnect failed:', e);
        }
    }, [connect, deactivateClient]);

    // Listen for token refresh events from TokenRefreshService
    useEffect(() => {
        return tokenRefreshEmitter.subscribe(() => {
            console.log('[WebSocket] Token refreshed, scheduling reconnect');

            // Clear any existing reconnect timeout
            if (reconnectTimeoutRef.current) {
                clearTimeout(reconnectTimeoutRef.current);
            }

            // Debounce reconnect: wait 1 second after token refresh
            // This handles cases where multiple refreshes happen quickly
            reconnectTimeoutRef.current = setTimeout(() => {
                reconnect();
                reconnectTimeoutRef.current = null;
            }, 1000);
        });
    }, [reconnect]);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            disconnect();
        };
    }, [disconnect]);

    const subscribe = useCallback(
        (destination, callback, headers = {}, options = { replace: false }) => {
            const client = stompClientRef.current;

            if (!client?.connected) {
                console.warn('[WebSocket] Cannot subscribe: not connected');
                return null;
            }

            const existing = subscriptionsRef.current.get(destination);
            if (existing) {
                if (options?.replace) {
                    try {
                        existing.subscription?.unsubscribe();
                    } catch {
                        /* noop */
                    }
                } else {
                    console.warn(`[WebSocket] Already subscribed to ${destination}`);
                    return null;
                }
            }

            const subscription = client.subscribe(destination, callback, headers);
            subscriptionsRef.current.set(destination, { callback, headers, subscription });

            // Return unsubscribe function
            return () => {
                try {
                    subscription.unsubscribe();
                } finally {
                    subscriptionsRef.current.delete(destination);
                }
            };
        },
        []
    );

    const unsubscribe = useCallback((destination) => {
        const subData = subscriptionsRef.current.get(destination);
        if (subData) {
            try {
                subData.subscription?.unsubscribe();
            } finally {
                subscriptionsRef.current.delete(destination);
            }
        }
    }, []);

    const enqueueMessage = (payload) => {
        const queue = messageQueueRef.current;
        if (queue.length >= MAX_QUEUE_SIZE) {
            queue.shift(); // Drop oldest
        }
        queue.push(payload);
    };

    const sendMessage = useCallback((destination, body, headers = {}) => {
        const payload = { destination, body, headers };
        const client = stompClientRef.current;

        if (client?.connected) {
            try {
                client.publish(payload);
            } catch {
                enqueueMessage(payload);
            }
        } else {
            enqueueMessage(payload);
            console.warn('[WebSocket] Queued message: not connected');
        }
    }, []);

    return {
        connect,
        disconnect,
        subscribe,
        unsubscribe,
        sendMessage,
        isConnected,
        isConnecting,
        client: stompClientRef,
    };
};

export default useWebSocket;