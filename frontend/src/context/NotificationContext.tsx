import React, { createContext, useContext, useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from './AuthContext';
import { NotificationItem } from '../types';
import api from '../services/api';
import { getWebSocketUrl } from '../services/websocket';

interface NotificationContextType {
  notifications: NotificationItem[];
  unreadCount: number;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  stompClient: Client | null;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [stompClient, setStompClient] = useState<Client | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setNotifications([]);
      return;
    }

    // Safely fetch existing notifications
    const fetchNotifications = async () => {
      try {
        const res = await api.get('/notifications');
        setNotifications(res.data.data?.content || []);
      } catch (err) {
        console.warn('Could not fetch notifications from backend API:', err);
      }
    };
    fetchNotifications();

    // Safely Setup STOMP client with resilient SockJS factory
    let client: Client | null = null;
    try {
      const token = localStorage.getItem('studyhub_access_token');
      const SockJSClass = (SockJS as any).default || SockJS;

      client = new Client({
        webSocketFactory: () => new SockJSClass(getWebSocketUrl('/ws-studyhub')),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: () => {}, // suppress noisy console traces
      });

      client.onConnect = () => {
        try {
          client?.subscribe('/user/queue/notifications', (message) => {
            try {
              const newNotification: NotificationItem = JSON.parse(message.body);
              setNotifications((prev) => [newNotification, ...prev]);
            } catch (e) {
              console.error('Failed to parse STOMP notification payload', e);
            }
          });
        } catch (subErr) {
          console.warn('Notification subscription warning:', subErr);
        }
      };

      client.onStompError = (frame) => {
        console.warn('STOMP broker error:', frame.headers['message']);
      };

      client.activate();
      setStompClient(client);
    } catch (wsErr) {
      console.warn('WebSocket connection init warning:', wsErr);
    }

    return () => {
      if (client) {
        try {
          client.deactivate();
        } catch (deactErr) {}
      }
    };
  }, [isAuthenticated, user]);

  const markAsRead = async (id: number) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (e) {
      console.error('Failed to mark notification as read', e);
    }
  };

  const markAllAsRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (e) {
      console.error('Failed to mark all notifications as read', e);
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <NotificationContext.Provider
      value={{ notifications, unreadCount, markAsRead, markAllAsRead, stompClient }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error('useNotifications must be used within a NotificationProvider');
  return context;
};
