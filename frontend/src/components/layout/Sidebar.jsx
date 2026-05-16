import { NavLink } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import {
  LayoutDashboard, ClipboardCheck, Users, BookOpen,
  Calendar, Cpu, BarChart2, LogOut
} from 'lucide-react'

const navItems = {
  ADMIN: [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/attendance', icon: ClipboardCheck, label: 'Attendance' },
    { to: '/students', icon: Users, label: 'Students' },
    { to: '/classes', icon: BookOpen, label: 'Classes' },
    { to: '/sessions', icon: Calendar, label: 'Sessions' },
    { to: '/devices', icon: Cpu, label: 'Devices' },
    { to: '/reports', icon: BarChart2, label: 'Reports' },
  ],
  TEACHER: [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/attendance', icon: ClipboardCheck, label: 'Attendance' },
    { to: '/sessions', icon: Calendar, label: 'Sessions' },
    { to: '/reports', icon: BarChart2, label: 'Reports' },
  ],
  STUDENT: [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  ],
  PARENT: [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  ],
}

export default function Sidebar() {
  const { role, user, logout } = useAuthStore()
  const items = navItems[role] || navItems.STUDENT

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-gray-900 text-white">
      <div className="px-6 py-5 border-b border-gray-700">
        <h1 className="text-lg font-bold text-blue-400">Attendance System</h1>
        <p className="text-xs text-gray-400 mt-1">{user?.firstName} {user?.lastName}</p>
        <span className="text-xs bg-blue-900 text-blue-300 px-2 py-0.5 rounded mt-1 inline-block">
          {role}
        </span>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {items.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="px-3 py-4 border-t border-gray-700">
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2 w-full rounded-lg text-sm text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
        >
          <LogOut size={18} />
          Logout
        </button>
      </div>
    </aside>
  )
}
