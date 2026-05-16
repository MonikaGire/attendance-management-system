import { useQuery } from '@tanstack/react-query'
import { reportApi } from '../api/reportApi'

export function useClassReport(classId, from, to, page, size) {
  return useQuery({
    queryKey: ['class-report', classId, from, to, page, size],
    queryFn: () => reportApi.getClassReport(classId, from, to, page, size).then((r) => r.data.data),
    enabled: !!classId && !!from && !!to,
  })
}

export function useDailyReport(date) {
  return useQuery({
    queryKey: ['daily-report', date],
    queryFn: () => reportApi.getDailyReport(date).then((r) => r.data.data),
    enabled: !!date,
  })
}
