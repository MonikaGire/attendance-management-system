import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ProtectedRoute from './components/layout/ProtectedRoute'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import AttendancePage from './pages/attendance/AttendancePage'
import StudentsPage from './pages/students/StudentsPage'
import StudentDetailPage from './pages/students/StudentDetailPage'
import ClassesPage from './pages/classes/ClassesPage'
import SessionsPage from './pages/sessions/SessionsPage'
import DevicesPage from './pages/devices/DevicesPage'
import ReportsPage from './pages/reports/ReportsPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30000,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route path="/" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />

          <Route path="/attendance" element={
            <ProtectedRoute roles={['ADMIN', 'TEACHER']}>
              <AttendancePage />
            </ProtectedRoute>
          } />

          <Route path="/students" element={
            <ProtectedRoute roles={['ADMIN', 'TEACHER']}>
              <StudentsPage />
            </ProtectedRoute>
          } />

          <Route path="/students/:id" element={
            <ProtectedRoute>
              <StudentDetailPage />
            </ProtectedRoute>
          } />

          <Route path="/classes" element={
            <ProtectedRoute roles={['ADMIN']}>
              <ClassesPage />
            </ProtectedRoute>
          } />

          <Route path="/sessions" element={
            <ProtectedRoute roles={['ADMIN', 'TEACHER']}>
              <SessionsPage />
            </ProtectedRoute>
          } />

          <Route path="/devices" element={
            <ProtectedRoute roles={['ADMIN']}>
              <DevicesPage />
            </ProtectedRoute>
          } />

          <Route path="/reports" element={
            <ProtectedRoute roles={['ADMIN', 'TEACHER']}>
              <ReportsPage />
            </ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
