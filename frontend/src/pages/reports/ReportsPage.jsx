import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { format, subDays } from 'date-fns'
import { reportApi } from '../../api/reportApi'
import { studentApi } from '../../api/studentApi'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import StatusBadge from '../../components/ui/StatusBadge'
import { Download, BarChart2 } from 'lucide-react'

export default function ReportsPage() {
  const [classId, setClassId] = useState('')
  const [from, setFrom] = useState(format(subDays(new Date(), 30), 'yyyy-MM-dd'))
  const [to, setTo] = useState(format(new Date(), 'yyyy-MM-dd'))
  const [trigger, setTrigger] = useState(false)

  const { data: classes } = useQuery({
    queryKey: ['classes'],
    queryFn: () => studentApi.getClasses().then((r) => r.data.data),
  })

  const { data: report, isLoading, refetch } = useQuery({
    queryKey: ['report', classId, from, to],
    queryFn: () => reportApi.getClassReport(classId, from, to).then((r) => r.data.data),
    enabled: trigger && !!classId,
  })

  const handleGenerate = () => {
    setTrigger(true)
    setTimeout(refetch, 100)
  }

  const handleExport = async () => {
    const res = await reportApi.exportCsv(classId, from, to)
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `attendance_${format(new Date(), 'yyyyMMdd')}.csv`)
    document.body.appendChild(link)
    link.click()
    link.remove()
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-gray-900">Reports</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Class</label>
            <select value={classId} onChange={(e) => setClassId(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm min-w-44">
              <option value="">Select Class</option>
              {(classes || []).map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">From</label>
            <input type="date" value={from} onChange={(e) => setFrom(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">To</label>
            <input type="date" value={to} onChange={(e) => setTo(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm" />
          </div>
          <button onClick={handleGenerate} disabled={!classId}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50">
            <BarChart2 size={16} /> Generate
          </button>
          {report && (
            <button onClick={handleExport}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700">
              <Download size={16} /> Export CSV
            </button>
          )}
        </div>
      </div>

      {isLoading && <LoadingSpinner />}

      {report && (
        <div className="space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[
              { label: 'Total Sessions', value: report.totalSessions },
              { label: 'Present %', value: `${report.presentPercent}%` },
              { label: 'Absent', value: report.absentCount },
              { label: 'Late', value: report.lateCount },
            ].map((s) => (
              <div key={s.label} className="bg-white rounded-xl border border-gray-200 p-4 text-center">
                <div className="text-2xl font-bold text-gray-900">{s.value}</div>
                <div className="text-xs text-gray-500 mt-1">{s.label}</div>
              </div>
            ))}
          </div>

          {report.topAbsentStudents?.length > 0 && (
            <div className="bg-white rounded-xl border border-gray-200 p-5">
              <h3 className="text-sm font-semibold text-gray-800 mb-3">Top 5 Most Absent Students</h3>
              <div className="space-y-2">
                {report.topAbsentStudents.map((s, i) => (
                  <div key={s.studentId} className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2">
                      <span className="text-gray-400">#{i + 1}</span>
                      <span className="text-gray-800">{s.studentName}</span>
                    </div>
                    <span className="font-semibold text-red-600">{s.absentCount} absences</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table className="min-w-full divide-y divide-gray-100 text-sm">
              <thead className="bg-gray-50">
                <tr>
                  {['Student', 'Date', 'Status', 'Check-in', 'Source'].map((h) => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {(report.records || []).map((r) => (
                  <tr key={r.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">{r.studentName}</td>
                    <td className="px-4 py-3">{r.sessionDate}</td>
                    <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
                    <td className="px-4 py-3">
                      {r.checkInTime ? format(new Date(r.checkInTime), 'HH:mm') : '-'}
                    </td>
                    <td className="px-4 py-3 text-gray-400">{r.source}</td>
                  </tr>
                ))}
                {(!report.records || report.records.length === 0) && (
                  <tr><td colSpan={5} className="py-6 text-center text-gray-400">No records</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
