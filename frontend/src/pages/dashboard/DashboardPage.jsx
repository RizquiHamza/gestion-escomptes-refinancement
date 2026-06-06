import { useEffect, useState } from 'react'
import { FiFileText, FiRepeat, FiCreditCard, FiUsers, FiBriefcase, FiTrendingUp, FiCheckCircle, FiClock, FiAlertCircle } from 'react-icons/fi'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { getDashboard } from '../../services/dashboardService'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const fmt = (n) => n == null ? '—' : Number(n).toLocaleString('fr-MA', { minimumFractionDigits: 2 }) + ' MAD'

function StatCard({ label, value, icon: Icon, color, sub }) {
  const colors = {
    emerald: { bg: 'bg-emerald-50',  icon: 'bg-emerald-600',  text: 'text-emerald-600' },
    teal:    { bg: 'bg-teal-50',     icon: 'bg-teal-600',     text: 'text-teal-600' },
    purple:  { bg: 'bg-purple-50',   icon: 'bg-purple-600',   text: 'text-purple-600' },
    amber:   { bg: 'bg-amber-50',    icon: 'bg-amber-500',    text: 'text-amber-500' },
  }
  const c = colors[color] || colors.emerald
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

const COLORS = ['#059669', '#0d9488', '#7c3aed', '#d97706']

export default function DashboardPage() {
  const [data, setData]       = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  useEffect(() => {
    getDashboard()
      .then(r => setData(r.data))
      .catch(() => setError('Impossible de charger le tableau de bord'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner text="Chargement du tableau de bord…" />
  if (error)   return <p className="text-center text-red-600 py-12">{error}</p>

  const barData = [
    { name: 'Montant escompté',       montant: Number(data.montantTotalEscomptes       || 0) },
    { name: 'Net reçu (UNIMAGEC)',    montant: Number(data.netRecuTotal                || 0) },
    { name: 'Montant refinancé',      montant: Number(data.montantTotalRefinancements  || 0) },
    { name: 'Total à rembourser',     montant: Number(data.totalRemboursement          || 0) },
  ]

  const pieData = [
    { name: 'Clients',        value: Number(data.totalClients       || 0) },
    { name: 'Fournisseurs',   value: Number(data.totalFournisseurs  || 0) },
    { name: 'Banques',        value: Number(data.totalBanques       || 0) },
    { name: 'Utilisateurs',   value: Number(data.totalUtilisateurs  || 0) },
  ]

  return (
    <div className="space-y-6">

      {/* ── Ligne 1 : compteurs principaux ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Escomptes"
          value={data.totalEscomptes}
          icon={FiFileText}
          color="emerald"
          sub={`Approuvés : ${data.escomptesApprouves ?? 0}  •  En attente : ${data.escomptesEnAttente ?? 0}`}
        />
        <StatCard
          label="Refinancements"
          value={data.totalRefinancements}
          icon={FiRepeat}
          color="teal"
          sub={`Approuvés : ${data.refinancementsApprouves ?? 0}  •  En attente : ${data.refinancementsEnAttente ?? 0}`}
        />
        <StatCard
          label="Clients"
          value={data.totalClients}
          icon={FiUsers}
          color="purple"
          sub="Partenaires de type CLIENT"
        />
        <StatCard
          label="Fournisseurs"
          value={data.totalFournisseurs}
          icon={FiBriefcase}
          color="amber"
          sub="Partenaires de type FOURNISSEUR"
        />
      </div>

      {/* ── Ligne 2 : synthèse financière ── */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">

        {/* Escomptes */}
        <div className="card space-y-3">
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-emerald-500" />
            <h2 className="text-base font-semibold text-gray-800">Escomptes approuvés</h2>
            <span className="text-xs text-gray-400 ml-auto">Opérations avec clients</span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="bg-gray-50 rounded-xl p-3">
              <p className="text-xs text-gray-500 font-medium mb-1 flex items-center gap-1">
                <FiTrendingUp className="h-3 w-3" /> Montant escompté
              </p>
              <p className="text-base font-bold text-gray-800">{fmt(data.montantTotalEscomptes)}</p>
            </div>
            <div className="bg-red-50 rounded-xl p-3">
              <p className="text-xs text-red-500 font-medium mb-1 flex items-center gap-1">
                <FiAlertCircle className="h-3 w-3" /> Agios bancaires
              </p>
              <p className="text-base font-bold text-red-700">{fmt(data.agiosTotaux)}</p>
            </div>
          </div>

          {/* Résultat mis en évidence */}
          <div className="rounded-xl p-4 text-white flex items-center justify-between"
               style={{ background: 'linear-gradient(135deg, #059669 0%, #047857 100%)' }}>
            <div>
              <p className="text-xs font-semibold text-emerald-100 uppercase tracking-wide mb-1">
                Net reçu par UNIMAGEC
              </p>
              <p className="text-2xl font-black">{fmt(data.netRecuTotal)}</p>
              <p className="text-[10px] text-emerald-200 mt-1">= Montant escompté − Agios bancaires</p>
            </div>
            <FiCheckCircle className="h-10 w-10 text-emerald-300 opacity-40 flex-shrink-0" />
          </div>
        </div>

        {/* Refinancements */}
        <div className="card space-y-3">
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-orange-500" />
            <h2 className="text-base font-semibold text-gray-800">Refinancements approuvés</h2>
            <span className="text-xs text-gray-400 ml-auto">Opérations avec fournisseurs</span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="bg-gray-50 rounded-xl p-3">
              <p className="text-xs text-gray-500 font-medium mb-1 flex items-center gap-1">
                <FiRepeat className="h-3 w-3" /> Montant financé
              </p>
              <p className="text-base font-bold text-gray-800">{fmt(data.montantTotalRefinancements)}</p>
            </div>
            <div className="bg-red-50 rounded-xl p-3">
              <p className="text-xs text-red-500 font-medium mb-1 flex items-center gap-1">
                <FiAlertCircle className="h-3 w-3" /> Intérêts bancaires
              </p>
              <p className="text-base font-bold text-red-700">{fmt(data.interetsTotaux)}</p>
            </div>
          </div>

          {/* Résultat mis en évidence */}
          <div className="rounded-xl p-4 text-white flex items-center justify-between"
               style={{ background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)' }}>
            <div>
              <p className="text-xs font-semibold text-orange-100 uppercase tracking-wide mb-1">
                Total à rembourser à la banque
              </p>
              <p className="text-2xl font-black">{fmt(data.totalRemboursement)}</p>
              <p className="text-[10px] text-orange-200 mt-1">= Montant financé + Intérêts bancaires</p>
            </div>
            <FiClock className="h-10 w-10 text-orange-300 opacity-40 flex-shrink-0" />
          </div>
        </div>
      </div>

      {/* ── Ligne 3 : graphiques ── */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">Flux financiers (MAD)</h2>
          <p className="text-xs text-gray-400 mb-4">Opérations approuvées uniquement</p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={barData} barSize={32}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 10 }} />
              <YAxis tick={{ fontSize: 10 }} />
              <Tooltip formatter={(v) => v.toLocaleString('fr-MA') + ' MAD'} />
              <Bar dataKey="montant" fill="#059669" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">Répartition des entités</h2>
          <p className="text-xs text-gray-400 mb-4">Clients, Fournisseurs, Banques, Utilisateurs</p>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={pieData} cx="50%" cy="45%" outerRadius={85} dataKey="value"
                label={({ name, value }) => `${name}: ${value}`} labelLine={false}>
                {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* ── Ligne 4 : alertes en attente ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <div className="card text-center">
          <FiClock className="h-5 w-5 text-yellow-500 mx-auto mb-2" />
          <p className="text-xs text-gray-500 mb-1">Escomptes en attente</p>
          <p className="text-2xl font-bold text-yellow-600">{data.escomptesEnAttente ?? 0}</p>
        </div>
        <div className="card text-center">
          <FiClock className="h-5 w-5 text-yellow-500 mx-auto mb-2" />
          <p className="text-xs text-gray-500 mb-1">Refinancements en attente</p>
          <p className="text-2xl font-bold text-yellow-600">{data.refinancementsEnAttente ?? 0}</p>
        </div>
        <div className="card text-center">
          <FiAlertCircle className="h-5 w-5 text-red-400 mx-auto mb-2" />
          <p className="text-xs text-gray-500 mb-1">Escomptes rejetés</p>
          <p className="text-2xl font-bold text-red-500">{data.escomptesRejetes ?? 0}</p>
        </div>
        <div className="card text-center">
          <FiCreditCard className="h-5 w-5 text-emerald-500 mx-auto mb-2" />
          <p className="text-xs text-gray-500 mb-1">Banques partenaires</p>
          <p className="text-2xl font-bold text-emerald-600">{data.totalBanques ?? 0}</p>
        </div>
      </div>

    </div>
  )
}
