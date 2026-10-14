import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import {
  Resource,
  ResourceRecommendation,
  Review,
  FocusSession,
  FocusStats,
  FocusLeaderboardEntry,
  Department,
  Subject,
  Topic,
  Doubt,
  AdminStats
} from '../types';

export const getBaseUrl = (): string => {
  const envUrl = (import.meta as any).env?.VITE_API_URL;
  if (envUrl && typeof envUrl === 'string' && envUrl.trim() !== '') {
    let url = envUrl.trim().replace(/\/+$/, '');
    if (!url.endsWith('/api/v1')) {
      url = `${url}/api/v1`;
    }
    return url;
  }
  // In production environment (Render static site or production build)
  if ((import.meta as any).env?.PROD || (typeof window !== 'undefined' && window.location.hostname.includes('render.com'))) {
    return 'https://studyhub-backend-hu9p.onrender.com/api/v1';
  }
  // Local development fallback to Vite proxy
  return '/api/v1';
};

export const BASE_URL = getBaseUrl();

export const getDownloadUrl = (resourceId: number | string): string => {
  return `${BASE_URL}/resources/${resourceId}/download`;
};

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('studyhub_access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url?.includes('/auth/')) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('studyhub_refresh_token');
      if (!refreshToken) {
        localStorage.removeItem('studyhub_access_token');
        localStorage.removeItem('studyhub_refresh_token');
        localStorage.removeItem('studyhub_user');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
        const { accessToken, refreshToken: newRefreshToken, user } = response.data.data;

        localStorage.setItem('studyhub_access_token', accessToken);
        localStorage.setItem('studyhub_refresh_token', newRefreshToken);
        if (user) localStorage.setItem('studyhub_user', JSON.stringify(user));

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }
        processQueue(null, accessToken);
        return api(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr as AxiosError, null);
        localStorage.removeItem('studyhub_access_token');
        localStorage.removeItem('studyhub_refresh_token');
        localStorage.removeItem('studyhub_user');
        window.location.href = '/login';
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Recommendations API
export const getRecommendations = async (): Promise<ResourceRecommendation[]> => {
  const res = await api.get('/recommendations');
  return res.data.data || [];
};

// Resources & Catalog API
export const getResources = async (params?: Record<string, any>): Promise<{ content: Resource[]; totalElements: number; totalPages: number }> => {
  const res = await api.get('/search/resources', { params });
  return res.data.data;
};

export const getResourceById = async (id: number | string): Promise<Resource> => {
  const res = await api.get(`/resources/${id}`);
  return res.data.data;
};

export const toggleBookmark = async (resourceId: number): Promise<boolean> => {
  const res = await api.post(`/resources/${resourceId}/bookmark`);
  return res.data.data?.bookmarked ?? true;
};

export const toggleUsefulVote = async (resourceId: number): Promise<boolean> => {
  const res = await api.post(`/resources/${resourceId}/useful`);
  return res.data.data?.votedUseful ?? true;
};

export const getMyResources = async (page = 0, size = 12) => {
  const res = await api.get('/resources/me', { params: { page, size } });
  return res.data.data;
};

export const getBookmarksList = async (page = 0, size = 20) => {
  const res = await api.get('/bookmarks', { params: { page, size } });
  return res.data.data;
};

// Reviews API
export const getResourceReviews = async (resourceId: number): Promise<Review[]> => {
  const res = await api.get(`/resources/${resourceId}/reviews`);
  return res.data.data || [];
};

export const submitReview = async (resourceId: number, data: { rating: number; text: string }): Promise<Review> => {
  const res = await api.post(`/resources/${resourceId}/reviews`, data);
  return res.data.data;
};

export const toggleHelpfulVote = async (reviewId: number): Promise<boolean> => {
  const res = await api.post(`/reviews/${reviewId}/helpful`);
  return res.data.data?.voted ?? true;
};

// Focus API
export const startFocusSession = async (data: { subjectId?: number; mode: string; plannedMinutes: number }): Promise<FocusSession> => {
  const res = await api.post('/focus/sessions/start', data);
  return res.data.data;
};

export const endFocusSession = async (sessionId: number): Promise<FocusSession> => {
  const res = await api.post(`/focus/sessions/${sessionId}/end`);
  return res.data.data;
};

export const getFocusStats = async (): Promise<FocusStats> => {
  const res = await api.get('/focus/stats');
  return res.data.data;
};

export const getFocusLeaderboard = async (departmentId?: number): Promise<FocusLeaderboardEntry[]> => {
  const res = await api.get('/focus/leaderboard', { params: { departmentId } });
  return res.data.data || [];
};

// Taxonomy API
export const getDepartments = async (): Promise<Department[]> => {
  const res = await api.get('/taxonomy/departments');
  return res.data.data || [];
};

export const getSubjects = async (departmentId?: number): Promise<Subject[]> => {
  const res = await api.get('/taxonomy/subjects', { params: { departmentId } });
  return res.data.data || [];
};

// Admin External Resource API
export const createExternalResource = async (data: any): Promise<Resource> => {
  const res = await api.post('/admin/resources/external', data);
  return res.data.data;
};

export default api;
