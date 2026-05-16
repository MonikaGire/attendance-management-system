import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { attendanceApi } from '../api/attendanceApi'

export function useDashboardSummary() {
  return useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: () => attendanceApi.getDashboardSummary().then((r) => r.data.data),
    refetchInterval: 30000,
  })
}

export function useSessionAttendance(sessionId) {
  return useQuery({
    queryKey: ['session-attendance', sessionId],
    queryFn: () => attendanceApi.getSessionAttendance(sessionId).then((r) => r.data.data),
    enabled: !!sessionId,
  })
}

export function useOverrideAttendance() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data) => attendanceApi.overrideAttendance(data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['session-attendance'] }),
  })
}

export function useRecentEvents() {
  return useQuery({
    queryKey: ['recent-events'],
    queryFn: () => attendanceApi.getRecent(10).then((r) => r.data.data),
    refetchInterval: 15000,
  })
}
