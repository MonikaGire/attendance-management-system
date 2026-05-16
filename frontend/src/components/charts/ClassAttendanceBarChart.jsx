import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer
} from 'recharts'

export default function ClassAttendanceBarChart({ data = [] }) {
  const formatted = data.map((d) => ({
    name: d.className,
    Present: d.presentCount,
    Absent: d.absentCount,
    Late: d.lateCount,
  }))

  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={formatted} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
        <XAxis dataKey="name" tick={{ fontSize: 12 }} />
        <YAxis tick={{ fontSize: 12 }} />
        <Tooltip />
        <Legend />
        <Bar dataKey="Present" fill="#3b82f6" radius={[3, 3, 0, 0]} />
        <Bar dataKey="Absent" fill="#ef4444" radius={[3, 3, 0, 0]} />
        <Bar dataKey="Late" fill="#f59e0b" radius={[3, 3, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
