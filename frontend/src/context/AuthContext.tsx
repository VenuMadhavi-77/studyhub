import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types';
import api from '../services/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<User>;
  register: (fullName: string, email: string, password: string, role?: string, departmentId?: number, semester?: number) => Promise<User>;
  updateProfile: (data: { fullName: string; departmentId?: number; semester?: number; goals?: string; preferredLanguage?: import('../types').ContentLanguage; level?: import('../types').ContentLevel }) => Promise<void>;
  refreshCurrentUser: () => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchCurrentUser = async () => {
    try {
      const token = localStorage.getItem('studyhub_access_token');
      if (!token) {
        setUser(null);
        setIsLoading(false);
        return;
      }
      const res = await api.get('/users/me');
      const profile = res.data.data;
      setUser(profile);
      localStorage.setItem('studyhub_user', JSON.stringify(profile));
    } catch (err) {
      console.warn('Could not refresh user session from /users/me', err);
      const stored = localStorage.getItem('studyhub_user');
      if (stored) {
        try {
          setUser(JSON.parse(stored));
        } catch (e) {}
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCurrentUser();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    const res = await api.post('/auth/login', { email, password });
    const authData = res.data.data;
    localStorage.setItem('studyhub_access_token', authData.accessToken);
    localStorage.setItem('studyhub_refresh_token', authData.refreshToken);
    
    // Fetch full profile with department and semester
    let currentUser: User = authData.user || {
      id: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
      status: 'ACTIVE',
      createdAt: new Date().toISOString()
    };

    try {
      const profileRes = await api.get('/users/me', {
        headers: { Authorization: `Bearer ${authData.accessToken}` }
      });
      currentUser = profileRes.data.data;
    } catch (e) {}

    localStorage.setItem('studyhub_user', JSON.stringify(currentUser));
    setUser(currentUser);
    return currentUser;
  };

  const register = async (
    fullName: string, 
    email: string, 
    password: string, 
    role: string = 'STUDENT',
    departmentId?: number,
    semester?: number
  ): Promise<User> => {
    const res = await api.post('/auth/register', { 
      fullName, 
      email, 
      password, 
      role,
      departmentId,
      semester 
    });
    const authData = res.data.data;
    localStorage.setItem('studyhub_access_token', authData.accessToken);
    localStorage.setItem('studyhub_refresh_token', authData.refreshToken);
    
    let currentUser: User = authData.user || {
      id: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
      departmentId,
      semester,
      status: 'ACTIVE',
      createdAt: new Date().toISOString()
    };

    try {
      const profileRes = await api.get('/users/me', {
        headers: { Authorization: `Bearer ${authData.accessToken}` }
      });
      currentUser = profileRes.data.data;
    } catch (e) {}

    localStorage.setItem('studyhub_user', JSON.stringify(currentUser));
    setUser(currentUser);
    return currentUser;
  };

  const updateProfile = async (data: { fullName: string; departmentId?: number; semester?: number }) => {
    const res = await api.put('/users/me', data);
    const updated = res.data.data;
    setUser(updated);
    localStorage.setItem('studyhub_user', JSON.stringify(updated));
  };

  const logout = () => {
    const refreshToken = localStorage.getItem('studyhub_refresh_token');
    if (refreshToken) {
      api.post('/auth/logout', { refreshToken }).catch(() => {});
    }
    localStorage.removeItem('studyhub_access_token');
    localStorage.removeItem('studyhub_refresh_token');
    localStorage.removeItem('studyhub_user');
    setUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      isAuthenticated: !!user, 
      isLoading, 
      login, 
      register, 
      updateProfile, 
      refreshCurrentUser: fetchCurrentUser, 
      logout 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
