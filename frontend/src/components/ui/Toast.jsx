import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Toast({ message, type = 'info', onClose }) {
  const colors = {
    success: 'bg-green-50 border-green-400 text-green-800',
    error: 'bg-red-50 border-red-400 text-red-800',
    info: 'bg-blue-50 border-blue-400 text-blue-800',
    warning: 'bg-amber-50 border-amber-400 text-amber-800',
  }

  useEffect(() => {
    const timer = setTimeout(onClose, 4000)
    return () => clearTimeout(timer)
  }, [onClose])

  return (
    <div className={`flex items-center justify-between p-3 border rounded-lg shadow-sm ${colors[type]}`}>
      <span className="text-sm">{message}</span>
      <button onClick={onClose} className="ml-4 opacity-60 hover:opacity-100">
        <X size={16} />
      </button>
    </div>
  )
}
