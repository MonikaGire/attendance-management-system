import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { format, subDays } from 'date-fns'
import { studentApi } from '../../api/studentApi'
import { attendanceApi } from '../../api/attendanceApi'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import StatusBadge from '../../components/ui/StatusBadge'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export default function StudentDetailPage() {
  const { id } = useParams()
  const from = format(subDays(new Date(), 30), 'yyyy-MM-dd')
  const to = format(new Date(), 'yyyy-MM-dd')

  const { data: student, isLoading } = useQuery({
    queryKey: ['student', id],
    queryFn: () => studentApi.getStudent(id).then((r) => r.data.data),
  })

  const { data: report } = useQuery({
    queryKey: ['student-attendance', id, from, to],
    queryFn: () => attendanceApi.getStudentReport(id, from, to, 0, 30).then((r) => r.data.data),
    enabled: !!id,
  })

  if (isLoading) return <LoadingSpinner />
  if (!student) return <div className="text-gray-500">Student not found</div>

  const records = report?.content || []
  const total = records.length
  const present = records.filter((r) => r.status === 'PRESENT' || r.status === 'LATE').length
  const rate = total > 0 ? ((present / total) * 100).toFixed(1) : 0

  const chartData = [...records].reverse().map((r) => ({
    date: format(new Date(r.sessionDate), 'MMM d'),
    value: r.status === 'PRESENT' || r.status === 'LATE' ? 1 : 0,
  }))

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Student Detail</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-6 flex gap-8">
        <div className="flex-1">
          <h2 className="text-xl font-semibold text-gray-900">
            {student.firstName} {student.lastName}
          </h2>
          <div className="mt-3 grid grid-cols-2 gap-3 text-sm text-gray-600">
            <div><span className="font-medium">Biometric ID:</span> {student.biometricId}</div>
            <div><span className="font-medium">Grade:</span> {student.grade}</div>
            <div><span className="font-medium">Phone:</span> {student.phone || '-'}</div>
            <div><span className="font-medium">Parent Phone:</span> {student.parentPhone || '-'}</div>
            <div><span className="font-medium">Enrolled:</span> {student.enrollmentDate}</div>
            <div><span className="font-medium">Status:</span> <StatusBadge status={student.status} /></div>
          </div>
        </div>
        <div className="text-center">
          <div className="text-5xl font-bold text-blue-600">{rate}%</div>
          <div className="text-sm text-gray-500 mt-1">Attendance Rate</div>
          <div className="text-xs text-gray-400">Last 30 days</div>
        </div>
      </div>

      {chartData.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-800 mb-3">Attendance Last 30 Days</h3>
          <ResponsiveContainer width="100%" height={120}>
            <LineChart data={chartData}>
              <XAxis dataKey="date" tick={{ fontSize: 10 }} interval="preserveStartEnd" />
              <YAxis domain={[0, 1]} tick={false} />
              <Tooltip formatter={(v) => (v === 1 ? 'Present' : 'Absent')} />
              <Line type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h3 className="text-sm font-semibold text-gray-800 mb-3">Attendance History</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm divide-y divide-gray-100">
            <thead>
              <tr className="text-xs text-gray-500 uppercase">
                <th className="py-2 px-3 text-left">Date</th>
                <th className="py-2 px-3 text-left">Class</th>
                <th className="py-2 px-3 text-left">Check-in</th>
                <th className="py-2 px-3 text-left">Status</th>
                <th className="py-2 px-3 text-left">Source</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {records.map((r) => (
                <tr key={r.id} className="hover:bg-gray-50">
                  <td className="py-2 px-3">{r.sessionDate}</td>
                  <td className="py-2 px-3">{r.className}</td>
                  <td className="py-2 px-3">
                    {r.checkInTime ? format(new Date(r.checkInTime), 'HH:mm') : '-'}
                  </td>
                  <td className="py-2 px-3"><StatusBadge status={r.status} /></td>
                  <td className="py-2 px-3 text-gray-400">{r.source}</td>
                </tr>
              ))}
              {records.length === 0 && (
                <tr><td colSpan={5} className="py-6 text-center text-gray-400">No records found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
