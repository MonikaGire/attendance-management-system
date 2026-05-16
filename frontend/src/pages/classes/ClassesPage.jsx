import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import api from '../../api/axios'
import { useAuthStore } from '../../store/authStore'
import { Plus, BookOpen } from 'lucide-react'

const schema = z.object({
  name: z.string().min(1, 'Required'),
  academicYear: z.string().min(1, 'Required'),
  room: z.string().optional(),
  teacherId: z.string().optional(),
})

export default function ClassesPage() {
  const { role } = useAuthStore()
  const [showModal, setShowModal] = useState(false)
  const queryClient = useQueryClient()

  const { data: classes, isLoading } = useQuery({
    queryKey: ['classes'],
    queryFn: () => api.get('/classes').then((r) => r.data.data),
  })

  const { data: teachers } = useQuery({
    queryKey: ['teachers'],
    queryFn: () => api.get('/users?role=TEACHER').then((r) => r.data.data).catch(() => []),
  })

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  })

  const create = useMutation({
    mutationFn: (d) => api.post('/classes', d),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['classes'] }); setShowModal(false); reset() },
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Classes</h1>
        {role === 'ADMIN' && (
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
          >
            <Plus size={16} /> Add Class
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="text-center py-8 text-gray-400">Loading...</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {(classes || []).map((c) => (
            <div key={c.id} className="bg-white rounded-xl border border-gray-200 p-5">
              <div className="flex items-start gap-3">
                <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
                  <BookOpen size={20} />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">{c.name}</h3>
                  <p className="text-xs text-gray-500 mt-1">{c.academicYear} · {c.room || 'No room'}</p>
                  {c.teacherName && (
                    <p className="text-xs text-gray-400 mt-1">Teacher: {c.teacherName}</p>
                  )}
                </div>
              </div>
            </div>
          ))}
          {(!classes || classes.length === 0) && (
            <p className="text-gray-400 col-span-3 text-center py-8">No classes found</p>
          )}
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-lg font-semibold mb-4">Add Class</h2>
            <form onSubmit={handleSubmit((d) => create.mutate(d))} className="space-y-3">
              <div>
                <label className="block text-xs font-medium mb-1">Class Name *</label>
                <input {...register('name')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                {errors.name && <p className="text-xs text-red-600">{errors.name.message}</p>}
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Academic Year *</label>
                <input {...register('academicYear')} className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="2024-25" />
                {errors.academicYear && <p className="text-xs text-red-600">{errors.academicYear.message}</p>}
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Room</label>
                <input {...register('room')} className="w-full border rounded-lg px-3 py-2 text-sm" />
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
