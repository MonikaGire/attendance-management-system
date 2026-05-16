import { useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { studentApi } from '../../api/studentApi'
import { useAuthStore } from '../../store/authStore'
import DataTable from '../../components/ui/DataTable'
import StatusBadge from '../../components/ui/StatusBadge'
import { Plus, Search } from 'lucide-react'

const schema = z.object({
  firstName: z.string().min(1, 'Required'),
  lastName: z.string().min(1, 'Required'),
  biometricId: z.string().min(1, 'Required'),
  enrollmentDate: z.string().min(1, 'Required'),
  phone: z.string().optional(),
  parentPhone: z.string().optional(),
  grade: z.string().optional(),
  whatsappConsent: z.boolean().optional(),
  classId: z.string().optional(),
})

export default function StudentsPage() {
  const { role } = useAuthStore()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [showModal, setShowModal] = useState(false)
  const [searchDebounced, setSearchDebounced] = useState('')
  const queryClient = useQueryClient()

  const debounce = useCallback((fn, delay) => {
    let timer
    return (...args) => {
      clearTimeout(timer)
      timer = setTimeout(() => fn(...args), delay)
    }
  }, [])

  const handleSearch = debounce((val) => setSearchDebounced(val), 300)

  const { data, isLoading } = useQuery({
    queryKey: ['students', searchDebounced, page],
    queryFn: () => studentApi.getStudents(searchDebounced, page, 20).then((r) => r.data.data),
  })

  const { data: classes } = useQuery({
    queryKey: ['classes'],
    queryFn: () => studentApi.getClasses().then((r) => r.data.data),
  })

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { whatsappConsent: true },
  })

  const createMutation = useMutation({
    mutationFn: (d) => studentApi.createStudent(d),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['students'] })
      setShowModal(false)
      reset()
    },
  })

  const columns = [
    {
      key: 'firstName',
      header: 'Name',
      render: (_, row) => (
        <Link to={`/students/${row.id}`} className="text-blue-600 hover:underline font-medium">
          {row.firstName} {row.lastName}
        </Link>
      ),
    },
    { key: 'biometricId', header: 'Biometric ID' },
    { key: 'grade', header: 'Grade' },
    { key: 'phone', header: 'Phone' },
    { key: 'parentPhone', header: 'Parent Phone' },
    { key: 'status', header: 'Status', render: (v) => <StatusBadge status={v} /> },
  ]

  const students = data?.content || []
  const totalPages = data?.totalPages || 0

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Students</h1>
        {(role === 'ADMIN' || role === 'TEACHER') && (
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
          >
            <Plus size={16} /> Add Student
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-4 flex items-center gap-3">
        <Search size={18} className="text-gray-400" />
        <input
          type="text"
          placeholder="Search by name or biometric ID..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); handleSearch(e.target.value) }}
          className="flex-1 outline-none text-sm"
        />
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <DataTable
          columns={columns}
          data={students}
          loading={isLoading}
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-lg shadow-xl overflow-y-auto max-h-screen">
            <h2 className="text-lg font-semibold mb-5">Add Student</h2>
            <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1">First Name *</label>
                  <input {...register('firstName')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                  {errors.firstName && <p className="text-xs text-red-600">{errors.firstName.message}</p>}
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1">Last Name *</label>
                  <input {...register('lastName')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                  {errors.lastName && <p className="text-xs text-red-600">{errors.lastName.message}</p>}
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1">Biometric ID *</label>
                  <input {...register('biometricId')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                  {errors.biometricId && <p className="text-xs text-red-600">{errors.biometricId.message}</p>}
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1">Grade</label>
                  <input {...register('grade')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1">Phone</label>
                  <input {...register('phone')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1">Parent Phone</label>
                  <input {...register('parentPhone')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Enrollment Date *</label>
                <input {...register('enrollmentDate')} type="date"
                  className="w-full border rounded-lg px-3 py-2 text-sm" />
                {errors.enrollmentDate && <p className="text-xs text-red-600">{errors.enrollmentDate.message}</p>}
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Class</label>
                <select {...register('classId')} className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="">None</option>
                  {(classes || []).map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className="flex items-center gap-2">
                <input {...register('whatsappConsent')} type="checkbox" id="consent" />
                <label htmlFor="consent" className="text-sm text-gray-700">WhatsApp Consent</label>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => { setShowModal(false); reset() }}
                  className="px-4 py-2 text-sm border rounded-lg hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={createMutation.isPending}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                  {createMutation.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
