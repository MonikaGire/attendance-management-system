import api from './axios'

export const reportApi = {
  getDailyReport: (date) => api.get('/reports/daily', { params: { date } }),
  getClassReport: (id, from, to, page = 0, size = 20) =>
    api.get(`/reports/class/${id}`, { params: { from, to, page, size } }),
  getStudentReport: (id, from, to) =>
    api.get(`/reports/student/${id}`, { params: { from, to } }),
  exportCsv: (classId, from, to) =>
    api.get('/reports/export/csv', {
      params: { classId, from, to },
      responseType: 'blob',
    }),
}
