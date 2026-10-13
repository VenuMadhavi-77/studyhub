import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { AppLayout } from './components/AppLayout';
import { ProtectedRoute } from './components/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { DashboardPage } from './pages/DashboardPage';
import { BrowsePage } from './pages/BrowsePage';
import { ResourceDetailPage } from './pages/ResourceDetailPage';
import { UploadResourcePage } from './pages/UploadResourcePage';
import { MyResourcesPage } from './pages/MyResourcesPage';
import { BookmarksPage } from './pages/BookmarksPage';
import { AIAssistantPage } from './pages/AIAssistantPage';
import { DoubtForumPage } from './pages/DoubtForumPage';
import { DoubtDetailPage } from './pages/DoubtDetailPage';
import { FocusPage } from './pages/FocusPage';
import { WhiteboardPage } from './pages/WhiteboardPage';
import { AdminPortalPage } from './pages/AdminPortalPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ProfilePage } from './pages/ProfilePage';

export const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <Router>
        <AuthProvider>
          <NotificationProvider>
            <Routes>
              {/* Layout Route with Navbar & Sidebar */}
              <Route element={<AppLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route
                  path="/dashboard"
                  element={
                    <ProtectedRoute>
                      <DashboardPage />
                    </ProtectedRoute>
                  }
                />
                <Route path="/browse" element={<BrowsePage />} />
                <Route path="/resources" element={<BrowsePage />} />
                <Route path="/resources/:id" element={<ResourceDetailPage />} />
                <Route
                  path="/upload"
                  element={
                    <ProtectedRoute>
                      <UploadResourcePage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/my-resources"
                  element={
                    <ProtectedRoute>
                      <MyResourcesPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/bookmarks"
                  element={
                    <ProtectedRoute>
                      <BookmarksPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/ai-assistant"
                  element={
                    <ProtectedRoute>
                      <AIAssistantPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/focus"
                  element={
                    <ProtectedRoute>
                      <FocusPage />
                    </ProtectedRoute>
                  }
                />
                <Route path="/doubts" element={<DoubtForumPage />} />
                <Route path="/doubts/:id" element={<DoubtDetailPage />} />
                <Route path="/whiteboard" element={<WhiteboardPage />} />
                <Route
                  path="/profile"
                  element={
                    <ProtectedRoute>
                      <ProfilePage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminPortalPage />
                    </ProtectedRoute>
                  }
                />
              </Route>

              {/* Public Auth Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </NotificationProvider>
        </AuthProvider>
      </Router>
    </ErrorBoundary>
  );
};

export default App;
