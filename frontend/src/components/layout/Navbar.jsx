import { Bell } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { useNotificationStore } from '../../store/notificationStore'

export default function Navbar() {
  const { user } = useAuthStore()
  const { notifications } = useNotificationStore()

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
      <div />
      <div className="flex items-center gap-4">
        <div className="relative">
          <Bell size={20} className="text-gray-600" />
          {notifications.length > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-4 h-4 rounded-full flex items-center justify-center">
              {notifications.length > 9 ? '9+' : notifications.length}
            </span>
          )}
        </div>
        <div className="text-sm text-gray-700">
          {user?.firstName} {user?.lastName}
        </div>
      </div>
    </header>
  )
}
