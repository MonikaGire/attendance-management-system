import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { format } from 'date-fns'
import api from '../../api/axios'
import { studentApi } from '../../api/studentApi'
import { useAuthStore } from '../../store/authStore'
import { Plus } from 'lucide-react'
import StatusBadge from '../../components/ui/StatusBadge'

const schema = z.object({
  classId: z.string().min(1, 'Required'),
  sessionDate: z.string().min(1, 'Required'),
  startTime: z.string().min(1, 'Required'),
  endTime: z.string().min(1, 'Required'),
  type: z.string().optional(),
  gracePeriodMinutes: z.coerce.number().optional(),
})

export default function SessionsPage() {
  const { role } = useAuthStore()
  const [showModal, setShowModal] = useState(false)
  const [filterDate, setFilterDate] = useState(format(new Date(), 'yyyy-MM-dd'))
  const queryClient = useQueryClient()

  const { data: classes } = useQuery({
    queryKey: ['classes'],
    queryFn: () => studentApi.getClasses().then((r) => r.data.data),
  })

  const { data: sessions, isLoading } = useQuery({
    queryKey: ['sessions-all', filterDate],
    queryFn: () => api.get('/sessions', { params: { date: filterDate } }).then((r) => r.data.data),
  })

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { type: 'REGULAR', gracePeriodMinutes: 15 },
  })

  const create = useMutation({
    mutationFn: (d) => api.post('/sessions', d),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['sessions-all'] }); setShowModal(false); reset() },
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Sessions</h1>
        {(role === 'ADMIN' || role === 'TEACHER') && (
          <button onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
            <Plus size={16} /> Add Session
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <label className="block text-xs font-medium text-gray-600 mb-1">Filter by Date</label>
        <input type="date" value={filterDate} onChange={(e) => setFilterDate(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm" />
      </div>

      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Class', 'Date', 'Start', 'End', 'Type', 'Grace Period', 'Status'].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {isLoading ? (
              <tr><td colSpan={7} className="text-center py-8 text-gray-400">Loading...</td></tr>
            ) : (sessions || []).map((s) => (
              <tr key={s.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{s.className}</td>
                <td className="px-4 py-3">{s.sessionDate}</td>
                <td className="px-4 py-3">{s.startTime}</td>
                <td className="px-4 py-3">{s.endTime}</td>
                <td className="px-4 py-3">{s.type}</td>
                <td className="px-4 py-3">{s.gracePeriodMinutes} min</td>
                <td className="px-4 py-3"><StatusBadge status={s.status} /></td>
              </tr>
            ))}
            {!isLoading && (!sessions || sessions.length === 0) && (
              <tr><td colSpan={7} className="py-8 text-center text-gray-400">No sessions found</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-lg font-semibold mb-4">Add Session</h2>
            <form onSubmit={handleSubmit((d) => create.mutate(d))} className="space-y-3">
              <div>
                <label className="block text-xs font-medium mb-1">Class *</label>
                <select {...register('classId')} className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="">Select</option>
                  {(classes || []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
                {errors.classId && <p className="text-xs text-red-600">{errors.classId.message}</p>}
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Date *</label>
                <input {...register('sessionDate')} type="date" className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1">Start Time *</label>
                  <input {...register('startTime')} type="time" className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1">End Time *</label>
                  <input {...register('endTime')} type="time" className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1">Type</label>
                  <select {...register('type')} className="w-full border rounded-lg px-3 py-2 text-sm">
                    {['REGULAR','EXAM','SPECIAL','MAKEUP'].map((t) => <option key={t}>{t}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1">Grace (min)</label>
                  <input {...register('gracePeriodMinutes')} type="number"
                    className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => { setShowModal(false); reset() }}
                  className="px-4 py-2 text-sm border rounded-lg">Cancel</button>
                <button type="submit" disabled={create.isPending}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg disabled:opacity-50">
                  {create.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
