import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { formatDistanceToNow } from 'date-fns'
import { deviceApi } from '../../api/deviceApi'
import { useWebSocket } from '../../hooks/useWebSocket'
import { Plus, Cpu, Copy, Check } from 'lucide-react'
import StatusBadge from '../../components/ui/StatusBadge'

const schema = z.object({
  name: z.string().min(1, 'Required'),
  location: z.string().optional(),
})

export default function DevicesPage() {
  const [showModal, setShowModal] = useState(false)
  const [newDeviceApiKey, setNewDeviceApiKey] = useState(null)
  const [copied, setCopied] = useState(false)
  const queryClient = useQueryClient()
  const { subscribe } = useWebSocket()

  const { data: devices, isLoading, refetch } = useQuery({
    queryKey: ['devices'],
    queryFn: () => deviceApi.getDevices().then((r) => r.data.data),
  })

  useEffect(() => {
    subscribe('/topic/devices', () => refetch())
  }, [])

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  })

  const register_ = useMutation({
    mutationFn: (d) => deviceApi.registerDevice(d),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['devices'] })
      setNewDeviceApiKey(res.data.data.apiKey)
      reset()
    },
  })

  const copyKey = () => {
    navigator.clipboard.writeText(newDeviceApiKey)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Devices</h1>
        <button onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> Register Device
        </button>
      </div>

      {isLoading ? <div className="text-center py-8 text-gray-400">Loading...</div> : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {(devices || []).map((d) => (
            <div key={d.id} className="bg-white rounded-xl border border-gray-200 p-5">
              <div className="flex items-start gap-3">
                <div className={`p-2 rounded-lg ${d.status === 'ACTIVE' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'}`}>
                  <Cpu size={20} />
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold text-gray-900">{d.name}</h3>
                    <StatusBadge status={d.status} />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">{d.location || 'No location'}</p>
                  <p className="text-xs text-gray-400 mt-2">
                    {d.lastHeartbeat
                      ? `Last seen ${formatDistanceToNow(new Date(d.lastHeartbeat))} ago`
                      : 'Never connected'}
                  </p>
                </div>
              </div>
            </div>
          ))}
          {(!devices || devices.length === 0) && (
            <p className="text-gray-400 col-span-3 text-center py-8">No devices registered</p>
          )}
        </div>
      )}

      {showModal && !newDeviceApiKey && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-lg font-semibold mb-4">Register Device</h2>
            <form onSubmit={handleSubmit((d) => register_.mutate(d))} className="space-y-3">
              <div>
                <label className="block text-xs font-medium mb-1">Device Name *</label>
                <input {...register('name')} className="w-full border rounded-lg px-3 py-2 text-sm" />
                {errors.name && <p className="text-xs text-red-600">{errors.name.message}</p>}
              </div>
              <div>
                <label className="block text-xs font-medium mb-1">Location</label>
                <input {...register('location')} className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)}
                  className="px-4 py-2 text-sm border rounded-lg">Cancel</button>
                <button type="submit" disabled={register_.isPending}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg disabled:opacity-50">
                  {register_.isPending ? 'Registering...' : 'Register'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {newDeviceApiKey && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-lg font-semibold mb-2 text-green-700">Device Registered!</h2>
            <p className="text-sm text-gray-600 mb-4">
              Save this API key — it will only be shown once.
            </p>
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-3 flex items-center gap-3">
              <code className="flex-1 text-xs break-all">{newDeviceApiKey}</code>
              <button onClick={copyKey} className="text-gray-500 hover:text-gray-700">
                {copied ? <Check size={16} className="text-green-600" /> : <Copy size={16} />}
              </button>
            </div>
            <div className="mt-5">
              <button onClick={() => { setNewDeviceApiKey(null); setShowModal(false) }}
                className="w-full py-2.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700">
                Done
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
