import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { format } from 'date-fns'
import { attendanceApi } from '../../api/attendanceApi'
import { studentApi } from '../../api/studentApi'
import { useWebSocket } from '../../hooks/useWebSocket'
import { useAuthStore } from '../../store/authStore'
import DataTable from '../../components/ui/DataTable'
import StatusBadge from '../../components/ui/StatusBadge'
import LoadingSpinner from '../../components/ui/LoadingSpinner'

export default function AttendancePage() {
  const { role } = useAuthStore()
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'yyyy-MM-dd'))
  const [selectedClass, setSelectedClass] = useState('')
  const [selectedSession, setSelectedSession] = useState('')
  const [overrideModal, setOverrideModal] = useState(null)
  const [overrideStatus, setOverrideStatus] = useState('')
  const [overrideNote, setOverrideNote] = useState('')
  const queryClient = useQueryClient()
  const { subscribe } = useWebSocket()

  const { data: classes } = useQuery({
    queryKey: ['classes'],
    queryFn: () => studentApi.getClasses().then((r) => r.data.data),
  })

  const { data: sessions } = useQuery({
    queryKey: ['sessions', selectedClass, selectedDate],
    queryFn: () => studentApi.getSessions(selectedClass, selectedDate).then((r) => r.data.data),
    enabled: !!selectedClass && !!selectedDate,
  })

  const { data: attendance, isLoading, refetch } = useQuery({
    queryKey: ['session-attendance', selectedSession],
    queryFn: () => attendanceApi.getSessionAttendance(selectedSession).then((r) => r.data.data),
    enabled: !!selectedSession,
  })

  const override = useMutation({
    mutationFn: (data) => attendanceApi.overrideAttendance(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['session-attendance'] })
      setOverrideModal(null)
    },
  })

  useEffect(() => {
    if (selectedSession) {
      subscribe(`/topic/attendance/${selectedClass}`, () => refetch())
    }
  }, [selectedSession, selectedClass])

  const columns = [
    { key: 'studentName', header: 'Student Name' },
    { key: 'biometricId', header: 'Biometric ID' },
    {
      key: 'checkInTime',
      header: 'Check-in Time',
      render: (v) => (v ? format(new Date(v), 'HH:mm:ss') : '-'),
    },
    {
      key: 'status',
      header: 'Status',
      render: (v) => <StatusBadge status={v} />,
    },
    { key: 'source', header: 'Source' },
    {
      key: 'actions',
      header: 'Actions',
      render: (_, row) =>
        (role === 'ADMIN' || role === 'TEACHER') ? (
          <button
            onClick={() => {
              setOverrideModal(row)
              setOverrideStatus(row.status)
              setOverrideNote(row.notes || '')
            }}
            className="text-xs text-blue-600 hover:underline"
          >
            Override
          </button>
        ) : null,
    },
  ]

  const records = attendance?.records || []

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-gray-900">Attendance</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-4 flex flex-wrap gap-4">
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Date</label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => { setSelectedDate(e.target.value); setSelectedSession('') }}
            className="border rounded-lg px-3 py-2 text-sm"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Class</label>
          <select
            value={selectedClass}
            onChange={(e) => { setSelectedClass(e.target.value); setSelectedSession('') }}
            className="border rounded-lg px-3 py-2 text-sm min-w-40"
          >
            <option value="">Select Class</option>
            {(classes || []).map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Session</label>
          <select
            value={selectedSession}
            onChange={(e) => setSelectedSession(e.target.value)}
            className="border rounded-lg px-3 py-2 text-sm min-w-48"
            disabled={!selectedClass}
          >
            <option value="">Select Session</option>
            {(sessions || []).map((s) => (
              <option key={s.id} value={s.id}>
                {s.startTime} – {s.endTime} ({s.status})
              </option>
            ))}
          </select>
        </div>
      </div>

      {selectedSession && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          {attendance && (
            <div className="flex gap-4 mb-4 text-sm text-gray-600">
              <span>Total: <strong>{attendance.totalStudents}</strong></span>
              <span className="text-green-700">Present: <strong>{attendance.presentCount}</strong></span>
              <span className="text-red-700">Absent: <strong>{attendance.absentCount}</strong></span>
              <span className="text-amber-700">Late: <strong>{attendance.lateCount}</strong></span>
            </div>
          )}
          <DataTable columns={columns} data={records} loading={isLoading} />
        </div>
      )}

      {overrideModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-lg font-semibold mb-4">Override Attendance</h2>
            <p className="text-sm text-gray-600 mb-4">
              Student: <strong>{overrideModal.studentName}</strong>
            </p>
            <div className="mb-4">
              <label className="block text-sm font-medium mb-1">New Status</label>
              <select
                value={overrideStatus}
                onChange={(e) => setOverrideStatus(e.target.value)}
                className="w-full border rounded-lg px-3 py-2 text-sm"
              >
                {['PRESENT', 'LATE', 'ABSENT', 'EXCUSED'].map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="mb-5">
              <label className="block text-sm font-medium mb-1">Notes</label>
              <textarea
                value={overrideNote}
                onChange={(e) => setOverrideNote(e.target.value)}
                rows={3}
                className="w-full border rounded-lg px-3 py-2 text-sm"
                placeholder="Reason for override..."
              />
            </div>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setOverrideModal(null)}
                className="px-4 py-2 text-sm border rounded-lg hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={() => override.mutate({
                  studentId: overrideModal.studentId,
                  sessionId: overrideModal.sessionId,
                  newStatus: overrideStatus,
                  notes: overrideNote,
                })}
                disabled={override.isPending}
                className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {override.isPending ? 'Saving...' : 'Save Override'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
