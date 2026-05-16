import api from './axios'

export const deviceApi = {
  getDevices: () => api.get('/devices'),
  getDevice: (id) => api.get(`/devices/${id}`),
  registerDevice: (data) => api.post('/devices', data),
  updateDevice: (id, data) => api.put(`/devices/${id}`, data),
  deleteDevice: (id) => api.delete(`/devices/${id}`),
}
