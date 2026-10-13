/**
 * WebSocket & SockJS URL configuration helper
 * Automatically selects wss:// or ws:// depending on API URL protocol or current window location
 */
export const getWebSocketUrl = (endpoint: string = '/ws-studyhub'): string => {
  const apiUrl = (import.meta as any).env?.VITE_API_URL || '';

  if (apiUrl.startsWith('https://')) {
    const host = apiUrl.replace(/^https:\/\//, '').replace(/\/api.*$/, '');
    return `https://${host}${endpoint}`;
  }

  if (apiUrl.startsWith('http://')) {
    const host = apiUrl.replace(/^http:\/\//, '').replace(/\/api.*$/, '');
    return `http://${host}${endpoint}`;
  }

  // Relative or localhost URL - mirror window protocol (https -> wss / https)
  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
    return `${protocol}//${window.location.host}${endpoint}`;
  }

  return endpoint;
};

export const getNativeWsUrl = (endpoint: string = '/ws-studyhub'): string => {
  const apiUrl = (import.meta as any).env?.VITE_API_URL || '';

  if (apiUrl.startsWith('https://')) {
    const host = apiUrl.replace(/^https:\/\//, '').replace(/\/api.*$/, '');
    return `wss://${host}${endpoint}`;
  }

  if (apiUrl.startsWith('http://')) {
    const host = apiUrl.replace(/^http:\/\//, '').replace(/\/api.*$/, '');
    return `ws://${host}${endpoint}`;
  }

  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${window.location.host}${endpoint}`;
  }

  return `ws://localhost:8080${endpoint}`;
};
