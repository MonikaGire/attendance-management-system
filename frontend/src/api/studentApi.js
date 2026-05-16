import api from './axios'

export const studentApi = {
  getStudents: (search = '', page = 0, size = 20) =>
    api.get('/students', { params: { search, page, size } }),
  getStudent: (id) => api.get(`/students/${id}`),
  createStudent: (data) => api.post('/students', data),
  updateStudent: (id, data) => api.put(`/students/${id}`, data),
  deleteStudent: (id) => api.delete(`/students/${id}`),
  getClasses: () => api.get('/classes'),
  getSessions: (classId, date) => api.get('/sessions', { params: { classId, date } }),
}
