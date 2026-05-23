import { useEffect, useState } from 'react'
import { FiFileText, FiRepeat, FiCreditCard, FiBriefcase, FiTrendingUp, FiCheckCircle } from 'react-icons/fi'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { getDashboard } from '../../services/dashboardService'
import LoadingSpinner from '../../components/common/LoadingSpinner'

function StatCard({ label, value, icon: Icon, color, sub }) {
  const colors = {
    blue:   { bg: 'bg-blue-50',   icon: 'bg-blue-600',   text: 'text-blue-600' },
    green:  { bg: 'bg-green-50',  icon: 'bg-green-600',  text: 'text-green-600' },
    purple: { bg: 'bg-purple-50', icon: 'bg-purple-600', text: 'text-purple-600' },
    orange: { bg: 'bg-orange-50', icon: 'bg-orange-500', text: 'text-orange-500' },
  }
  const c = colors[color] || colors.blue
  return (
    <div className="card flex items-start gap-4">
      <div className={`${c.bg} p-3 rounded-xl`}>
        <Icon className={`h-6 w-6 ${c.text}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm text-gray-500 mb-1">{label}</p>
        <p className="text-2xl font-bold text-gray-900 truncate">{value ?? '—'}</p>
        {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
      </div>
    </div>
  )
}

const COLORS = ['#2563eb', '#16a34a', '#dc2626', '#d97706']

const fmt = (n) => n == null ? '—' : Number(n).toLocaleString('fr-MA', { minimumFractionDigits: 2 }) + ' MAD'

export default function DashboardPage() {
  const [data, setData]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState(null)

  useEffect(() => {
    getDashboard()
      .then(r => setData(r.data))
      .catch(() => setError('Impossible de charger le tableau de bord'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner text="Chargement du tableau de bord…" />
  if (error)   return <p className="text-center text-red-600 py-12">{error}</p>

  const pieData = [
    { name: 'Escomptes',      value: data.totalEscomptes       || 0 },
    { name: 'Refinancements', value: data.totalRefinancements  || 0 },
    { name: 'Partenaires',    value: data.totalPartenaires     || 0 },
    { name: 'Banques',        value: data.totalBanques         || 0 },
  ]

  const barData = [
    { name: 'Escomptes approuvés',      montant: Number(data.montantTotalEscomptesApprouves || 0) },
    { name: 'Refinancements approuvés', montant: Number(data.montantTotalRefinancementsApprouves || 0) },
  ]

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard label="Total Escomptes"      value={data.totalEscomptes}      icon={FiFileText}   color="blue"   sub={`Approuvés : ${data.escomptesApprouves ?? 0}`} />
        <StatCard label="Total Refinancements" value={data.totalRefinancements}  icon={FiRepeat}     color="green"  sub={`Approuvés : ${data.refinancementsApprouves ?? 0}`} />
        <StatCard label="Partenaires"          value={data.totalPartenaires}     icon={FiBriefcase}  color="purple" />
        <StatCard label="Banques"              value={data.totalBanques}         icon={FiCreditCard} color="orange" />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">Montants approuvés (MAD)</h2>
          <p className="text-xs text-gray-400 mb-4">Cumul des opérations approuvées</p>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="bg-blue-50 rounded-xl p-4">
              <FiTrendingUp className="h-5 w-5 text-blue-600 mb-2" />
              <p className="text-xs text-blue-600 font-medium">Escomptes</p>
              <p className="text-lg font-bold text-blue-800 mt-1 break-all">{fmt(data.montantTotalEscomptesApprouves)}</p>
            </div>
            <div className="bg-green-50 rounded-xl p-4">
              <FiCheckCircle className="h-5 w-5 text-green-600 mb-2" />
              <p className="text-xs text-green-600 font-medium">Refinancements</p>
              <p className="text-lg font-bold text-green-800 mt-1 break-all">{fmt(data.montantTotalRefinancementsApprouves)}</p>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={barData} barSize={40}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v) => v.toLocaleString('fr-MA') + ' MAD'} />
              <Bar dataKey="montant" fill="#2563eb" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">Répartition des entités</h2>
          <p className="text-xs text-gray-400 mb-4">Compteurs par type</p>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie data={pieData} cx="50%" cy="45%" outerRadius={100} dataKey="value" label={({ name, value }) => `${name}: ${value}`} labelLine={false}>
                {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <div className="card text-center">
          <p className="text-xs text-gray-500 mb-1">Agios escomptes approuvés</p>
          <p className="text-xl font-bold text-gray-900">{fmt(data.agiosTotalApprouves)}</p>
        </div>
        <div className="card text-center">
          <p className="text-xs text-gray-500 mb-1">Intérêts refinancements approuvés</p>
          <p className="text-xl font-bold text-gray-900">{fmt(data.interetsTotalApprouves)}</p>
        </div>
        <div className="card text-center">
          <p className="text-xs text-gray-500 mb-1">Escomptes en attente</p>
          <p className="text-xl font-bold text-yellow-600">{data.escomptesEnAttente ?? 0}</p>
        </div>
        <div className="card text-center">
          <p className="text-xs text-gray-500 mb-1">Refinancements en attente</p>
          <p className="text-xl font-bold text-yellow-600">{data.refinancementsEnAttente ?? 0}</p>
        </div>
      </div>
    </div>
  )
}
