import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  user: null,
  accessToken: null,
  isAuthenticated: false,
  role: null,

  login: (authData) => {
    sessionStorage.setItem('refreshToken', authData.refreshToken)
    set({
      user: {
        id: authData.userId,
        email: authData.email,
        firstName: authData.firstName,
        lastName: authData.lastName,
      },
      accessToken: authData.accessToken,
      isAuthenticated: true,
      role: authData.role,
    })
  },

  logout: () => {
    sessionStorage.removeItem('refreshToken')
    set({ user: null, accessToken: null, isAuthenticated: false, role: null })
  },

  setTokens: (accessToken, refreshToken) => {
    if (refreshToken) sessionStorage.setItem('refreshToken', refreshToken)
    set({ accessToken })
  },
}))
