import {useCallback, useEffect, useRef, useState} from 'react';
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';
import apiService from '../Services/ApiService.js';

const websocketUrl = import.meta.env.VITE_API_WEBSOCKET_URL;
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

    // Token refresh listener tracking
    const tokenRefreshIntervalRef = useRef(null);

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
                if (retryCount < 3) { // Max 3 retries
                    failed.push({ destination, body, headers, retryCount: retryCount + 1 });
                } else {
                    console.error('[STOMP] Dropped message after max retries:', destination);
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
                // console.log(`[STOMP] Resubscribed to ${destination}`);
            } catch (e) {
                console.error(`[STOMP] Failed to resubscribe to ${destination}`, e);
            }
        });
    }, []);

    const connect = useCallback(() => {
        // Return existing in-flight promise if any
        if (pendingConnectPromiseRef.current) return pendingConnectPromiseRef.current;

        if (stompClientRef.current?.connected) {
            return Promise.resolve(stompClientRef.current);
        }

        const attemptId = ++connectAttemptIdRef.current;

        const promise = new Promise((resolve, reject) => {
            setIsConnecting(true);

            // If there is an old client, deactivate it first
            if (stompClientRef.current) {
                try {
                    stompClientRef.current.deactivate();
                } catch {
                    /* noop */
                }
            }

            const socket = new SockJS(websocketUrl);
            const client = new Client({
                webSocketFactory: () => socket,
                debug: (msg) => console.log('[STOMP DEBUG]', msg),
                reconnectDelay: 5000,

                heartbeatIncoming: 10000,
                heartbeatOutgoing: 10000,

                onConnect: () => {
                    // Ignore stale connects
                    if (attemptId !== connectAttemptIdRef.current) return;

                    setIsConnected(true);
                    setIsConnecting(false);

                    resubscribeAll(client);
                    flushQueue();

                    resolve(client);
                },

                onDisconnect: () => {
                    // Only reflect status for the latest attempt
                    if (attemptId === connectAttemptIdRef.current) {
                        setIsConnected(false);
                        setIsConnecting(false);
                    }
                },

                onStompError: (frame) => {
                    console.error('[STOMP] Error:', frame.headers?.message);
                    console.error('Details:', frame.body);
                    if (attemptId === connectAttemptIdRef.current) {
                        setIsConnecting(false);
                    }
                    reject(new Error(frame.headers?.message || 'STOMP connection error'));
                },
            });

            stompClientRef.current = client;
            client.activate();
        })
            .finally(() => {
                // Clear only if this is still the active promise
                if (pendingConnectPromiseRef.current?.finally) {
                    pendingConnectPromiseRef.current = null;
                }
            });

        pendingConnectPromiseRef.current = promise;
        return promise;
    }, [flushQueue, resubscribeAll]);

    const disconnect = useCallback(() => {
        // When explicitly disconnecting, also clear subscriptions (caller can re-add)
        subscriptionsRef.current.forEach((sub) => {
            try {
                sub.subscription?.unsubscribe();
            } catch {
                /* noop */
            }
        });
        subscriptionsRef.current.clear();

        try {
            stompClientRef.current?.deactivate();
        } finally {
            stompClientRef.current = null;
            setIsConnected(false);
            setIsConnecting(false);
        }
    }, []);

    const subscribe = useCallback(
        (destination, callback, headers = {}, options = { replace: false }) => {
            const client = stompClientRef.current;

            if (!client?.connected) {
                console.warn('[STOMP] Tried to subscribe before connection');
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
                    console.warn(`[STOMP] Already subscribed to ${destination}`);
                    return null;
                }
            }

            const subscription = client.subscribe(destination, callback, headers);
            subscriptionsRef.current.set(destination, { callback, headers, subscription });
            // console.log(`[STOMP] Subscribed to ${destination}`);

            // Return unsubscribe function
            return () => {
                try {
                    subscription.unsubscribe();
                } finally {
                    subscriptionsRef.current.delete(destination);
                    // console.log(`[STOMP] Unsubscribed from ${destination}`);
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
            queue.shift(); // drop oldest to make space
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
                // If publish fails due to a transient issue, queue it
                enqueueMessage(payload);
            }
        } else {
            // Queue for later
            enqueueMessage(payload);
            console.warn('[STOMP] Queued message because client not connected');
        }
    }, []);

    // Helper to gracefully deactivate the current client and await onDisconnect
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

            // Safety timer
            setTimeout(() => {
                if (!resolved) {
                    resolved = true;
                    resolve();
                }
            }, 3000);
        });
    }, []);

    const refreshTokenAndReconnect = useCallback(async () => {
        try {
            // Call the refresh endpoint to get new access token
            await apiService.post('/auth/refresh');

            // Disconnect current client but keep subscription map to resubscribe
            await deactivateClient();
            await connect();

            console.log('[WS] Successfully reconnected after token refresh');
        } catch (e) {
            console.error('[WS] Reconnect after token refresh failed:', e);
        }
    }, [connect, deactivateClient]);

    useEffect(() => {
        // Set up periodic token refresh check
        // Refresh every 25 minutes (token expires in 30 minutes)
        tokenRefreshIntervalRef.current = setInterval(() => {
            refreshTokenAndReconnect();
        }, 25 * 60 * 1000); // 25 minutes

        return () => {
            if (tokenRefreshIntervalRef.current) {
                clearInterval(tokenRefreshIntervalRef.current);
            }
            disconnect();
        };
    }, [disconnect, refreshTokenAndReconnect]);

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