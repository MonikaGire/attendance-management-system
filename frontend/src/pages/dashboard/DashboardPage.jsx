import { useEffect } from 'react'
import { Users, UserX, Clock, Cpu } from 'lucide-react'
import { useDashboardSummary } from '../../hooks/useAttendance'
import { useWebSocket } from '../../hooks/useWebSocket'
import StatCard from '../../components/ui/StatCard'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import AttendanceTrendChart from '../../components/charts/AttendanceTrendChart'
import ClassAttendanceBarChart from '../../components/charts/ClassAttendanceBarChart'
import StatusBadge from '../../components/ui/StatusBadge'
import { format } from 'date-fns'

export default function DashboardPage() {
  const { data: summary, isLoading, refetch } = useDashboardSummary()
  const { subscribe } = useWebSocket()

  useEffect(() => {
    subscribe('/topic/attendance/*', () => refetch())
  }, [])

  if (isLoading) return <LoadingSpinner />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500">
          {format(new Date(), 'EEEE, MMMM d, yyyy')}
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Attendance Rate"
          value={`${summary?.attendanceRate?.toFixed(1) || 0}%`}
          subtitle="Today"
          icon={Users}
          color="blue"
        />
        <StatCard
          title="Total Absent"
          value={summary?.totalAbsent || 0}
          subtitle="Today"
          icon={UserX}
          color="red"
        />
        <StatCard
          title="Total Late"
          value={summary?.totalLate || 0}
          subtitle="Today"
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="Active Devices"
          value={summary?.activeDevices || 0}
          subtitle="Online"
          icon={Cpu}
          color="green"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-base font-semibold text-gray-900 mb-4">7-Day Attendance Trend</h2>
          <AttendanceTrendChart data={summary?.trend || []} />
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Class Attendance Today</h2>
          <ClassAttendanceBarChart data={summary?.classSummaries || []} />
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-base font-semibold text-gray-900 mb-4">Recent Events</h2>
        <div className="space-y-2">
          {(summary?.recentEvents || []).map((event) => (
            <div
              key={event.id}
              className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0"
            >
              <div>
                <span className="font-medium text-gray-800">{event.studentName}</span>
                <span className="text-gray-400 text-xs ml-2">
                  {event.className} · {event.biometricId}
                </span>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={event.status} />
                <span className="text-xs text-gray-400">
                  {event.checkInTime
                    ? format(new Date(event.checkInTime), 'HH:mm')
                    : '-'}
                </span>
              </div>
            </div>
          ))}
          {(!summary?.recentEvents || summary.recentEvents.length === 0) && (
            <p className="text-sm text-gray-400 text-center py-4">No events today</p>
          )}
        </div>
      </div>
    </div>
  )
}
