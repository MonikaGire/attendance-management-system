import api from './axios'

export const attendanceApi = {
  getSessionAttendance: (sessionId) => api.get(`/attendance/session/${sessionId}`),
  overrideAttendance: (data) => api.post('/attendance/override', data),
  getStudentReport: (studentId, from, to, page = 0, size = 20) =>
    api.get(`/attendance/student/${studentId}`, { params: { from, to, page, size } }),
  getRecent: (limit = 10) => api.get('/attendance/recent', { params: { limit } }),
  getDashboardSummary: () => api.get('/dashboard/summary'),
  getTrend: (days = 7) => api.get('/dashboard/trend', { params: { days } }),
  getClassSummary: () => api.get('/dashboard/class-summary'),
}
