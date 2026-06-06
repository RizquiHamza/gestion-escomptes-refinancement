import { useEffect, useState } from 'react'
import {
  FiRefreshCw, FiDownload, FiPrinter,
  FiTrendingUp, FiAlertCircle, FiXCircle, FiCheckCircle,
} from 'react-icons/fi'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, Legend,
} from 'recharts'
import { getStatistiques } from '../../services/statistiquesService'
import LoadingSpinner from '../../components/common/LoadingSpinner'

// ── Formatage ─────────────────────────────────────────────────────────────────
const fmt = (n) =>
  n == null ? '—' : Number(n).toLocaleString('fr-MA', { minimumFractionDigits: 2 }) + ' MAD'
const fmtShort = (v) => {
  const n = Number(v)
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + ' M'
  if (n >= 1_000)     return (n / 1_000).toFixed(0) + ' k'
  return String(n)
}

// ── Couleurs ──────────────────────────────────────────────────────────────────
const BANK_COLORS = ['#059669', '#0d9488', '#7c3aed', '#d97706', '#3b82f6', '#ec4899']

// ── Tooltip personnalisé montants ─────────────────────────────────────────────
function TooltipMAD({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-white border border-gray-100 rounded-xl shadow-lg px-4 py-3 text-xs space-y-1">
      <p className="font-semibold text-gray-700">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>
          {p.name} : {Number(p.value).toLocaleString('fr-MA')} MAD
        </p>
      ))}
    </div>
  )
}

function TooltipCount({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-white border border-gray-100 rounded-xl shadow-lg px-4 py-3 text-xs space-y-1">
      <p className="font-semibold text-gray-700">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>{p.name} : {p.value}</p>
      ))}
    </div>
  )
}

// ── Jauge de taux ─────────────────────────────────────────────────────────────
function TauxCard({ label, taux, approuves, total, color }) {
  const pct = Math.min(100, Number(taux))
  const colors = {
    emerald: { bar: 'bg-emerald-500', text: 'text-emerald-600', light: 'bg-emerald-50' },
    orange:  { bar: 'bg-orange-500',  text: 'text-orange-600',  light: 'bg-orange-50'  },
  }
  const c = colors[color] || colors.emerald
  return (
    <div className="card">
      <p className="text-sm text-gray-500 mb-2">{label}</p>
      <div className="flex items-end gap-3 mb-3">
        <p className={`text-3xl font-black ${c.text}`}>{pct} %</p>
        <p className="text-xs text-gray-400 mb-1 leading-tight">
          {approuves} approuvés<br />sur {total} total
        </p>
      </div>
      <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
        <div
          className={`h-full ${c.bar} rounded-full transition-all duration-700`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

// ── Export CSV ────────────────────────────────────────────────────────────────
function exportCSV(data) {
  const date = new Date().toLocaleDateString('fr-FR')
  const rows = [
    ['Rapport Statistiques & Rapports — UNIMAGEC', date],
    [],
    ['TAUX DE PERFORMANCE'],
    ["Taux d'approbation Escomptes (%)", data.tauxApprobationEscomptes],
    ["Taux d'approbation Refinancements (%)", data.tauxApprobationRefinancements],
    [],
    ['ANOMALIES'],
    ['Escomptes rejetés',       data.escomptesRejetes],
    ['Escomptes annulés',       data.escomptesAnnules],
    ['Refinancements rejetés',  data.refinancementsRejetes],
    ['Refinancements annulés',  data.refinancementsAnnules],
    [],
    ['ESCOMPTES APPROUVÉS PAR BANQUE', 'Dossiers', 'Montant total (MAD)', 'Agios (MAD)'],
    ...(data.escomptesParBanque || []).map(b =>
      [b.banqueNom, b.count, b.montantTotal, b.chargesTotal]
    ),
    [],
    ['REFINANCEMENTS APPROUVÉS PAR BANQUE', 'Dossiers', 'Montant total (MAD)', 'Intérêts (MAD)'],
    ...(data.refinancementsParBanque || []).map(b =>
      [b.banqueNom, b.count, b.montantTotal, b.chargesTotal]
    ),
    [],
    ['ÉVOLUTION MENSUELLE', 'Escomptes créés', 'Refinancements créés',
     'Montant escomptes (MAD)', 'Montant refinancements (MAD)'],
    ...(data.evolutionMensuelle || []).map(m => [
      `${m.label} ${m.annee}`,
      m.countEscomptes,
      m.countRefinancements,
      m.montantEscomptes,
      m.montantRefinancements,
    ]),
  ]
  const csv  = rows.map(r => r.join(';')).join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `rapport-unimagec-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

// ── Impression rapport professionnel ─────────────────────────────────────────
function imprimerRapport(data) {
  const fmtP = (n) =>
    n == null ? '—' : Number(n).toLocaleString('fr-MA', { minimumFractionDigits: 2 }) + ' MAD'

  const dateStr = new Date().toLocaleDateString('fr-FR', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  })
  const dateShort = new Date().toLocaleDateString('fr-FR')

  const rowBanque = (list, chargeLabel) =>
    list.length === 0
      ? `<tr><td colspan="4" style="text-align:center;color:#9ca3af;padding:12px">Aucune donnée</td></tr>`
      : list.map(b => `
          <tr>
            <td>${b.banqueNom}</td>
            <td style="text-align:center">${b.count}</td>
            <td style="text-align:right;font-weight:600;color:#059669">${fmtP(b.montantTotal)}</td>
            <td style="text-align:right;color:#dc2626">${fmtP(b.chargesTotal)}</td>
          </tr>`).join('')

  const html = `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Rapport UNIMAGEC — ${dateShort}</title>
  <style>
    *{margin:0;padding:0;box-sizing:border-box}
    body{font-family:Arial,sans-serif;font-size:11px;color:#111;background:#fff}

    /* ─ En-tête ─ */
    .header{background:linear-gradient(135deg,#164d2e 0%,#2d6a4f 100%);color:#fff;padding:28px 36px;display:flex;justify-content:space-between;align-items:flex-end}
    .header-left h1{font-size:24px;font-weight:900;letter-spacing:1px}
    .header-left p{font-size:11px;opacity:.65;margin-top:4px}
    .header-right{text-align:right}
    .header-right .label{font-size:9px;opacity:.55;text-transform:uppercase;letter-spacing:.8px}
    .header-right .date{font-size:12px;font-weight:700;margin-top:2px}
    .header-right .confidential{margin-top:6px;font-size:8px;background:rgba(255,255,255,.15);padding:3px 8px;border-radius:4px;display:inline-block}

    /* ─ Corps ─ */
    .body{padding:28px 36px}

    /* ─ Section ─ */
    .section{margin-bottom:24px}
    .section-title{font-size:12px;font-weight:800;color:#164d2e;text-transform:uppercase;letter-spacing:.6px;border-bottom:2px solid #164d2e;padding-bottom:5px;margin-bottom:14px}

    /* ─ KPIs ─ */
    .kpi-row{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-bottom:14px}
    .kpi{border:1px solid #e5e7eb;border-radius:8px;padding:12px 14px}
    .kpi-label{font-size:9px;color:#6b7280;text-transform:uppercase;letter-spacing:.5px}
    .kpi-value{font-size:20px;font-weight:900;color:#164d2e;margin-top:3px}
    .kpi-sub{font-size:8px;color:#9ca3af;margin-top:2px}

    /* ─ Tableaux ─ */
    table{width:100%;border-collapse:collapse}
    thead tr{background:#f3f4f6}
    th{padding:8px 10px;font-size:9px;text-transform:uppercase;letter-spacing:.5px;color:#6b7280;font-weight:700}
    td{padding:7px 10px;border-bottom:1px solid #f3f4f6;font-size:10px;vertical-align:middle}
    tr:last-child td{border-bottom:none}
    .table-green thead tr{background:#f0fdf4}
    .table-green thead th{color:#059669}
    .table-orange thead tr{background:#fff7ed}
    .table-orange thead th{color:#ea580c}

    /* ─ 2 colonnes ─ */
    .two-col{display:grid;grid-template-columns:1fr 1fr;gap:16px}

    /* ─ Récapitulatif financier ─ */
    .fin-card{border-radius:8px;overflow:hidden}
    .fin-card table{margin:0}
    .fin-card .fin-total td{font-weight:800;font-size:12px}
    .fin-total-esc{background:#f0fdf4}
    .fin-total-ref{background:#fff7ed}

    /* ─ Évolution mensuelle ─ */
    .month-table thead tr{background:#e8f5e9}
    .month-table thead th{color:#2e7d32}

    /* ─ Séparateur ─ */
    .divider{border:none;border-top:1px solid #e5e7eb;margin:20px 0}

    /* ─ Pied de page ─ */
    .footer{margin-top:28px;border-top:1px solid #e5e7eb;padding-top:12px;text-align:center;color:#9ca3af;font-size:8.5px}
    .footer strong{color:#6b7280}

    /* ─ Impression ─ */
    @media print{
      body{print-color-adjust:exact;-webkit-print-color-adjust:exact}
      @page{margin:1.2cm;size:A4}
      .page-break{page-break-before:always;margin-top:0}
    }
  </style>
</head>
<body>

<!-- ═══ EN-TÊTE ═══ -->
<div class="header">
  <div class="header-left">
    <h1>UNIMAGEC</h1>
    <p>Univers du Matériel Agricole et du Commerce</p>
    <p style="margin-top:8px;font-size:13px;font-weight:700;opacity:.9">Rapport Statistiques &amp; Analyses Financières</p>
  </div>
  <div class="header-right">
    <div class="label">Date d'édition</div>
    <div class="date">${dateStr}</div>
    <div class="confidential">Document confidentiel</div>
  </div>
</div>

<div class="body">

  <!-- ═══ 1. INDICATEURS CLÉS ═══ -->
  <div class="section">
    <div class="section-title">1. Indicateurs Clés de Performance</div>
    <div class="kpi-row">
      <div class="kpi">
        <div class="kpi-label">Total Escomptes</div>
        <div class="kpi-value">${data.totalEscomptes}</div>
        <div class="kpi-sub">En attente : ${data.escomptesEnAttente}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">Total Refinancements</div>
        <div class="kpi-value">${data.totalRefinancements}</div>
        <div class="kpi-sub">En attente : ${data.refinancementsEnAttente}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">Taux approbation Esc.</div>
        <div class="kpi-value">${data.tauxApprobationEscomptes} %</div>
        <div class="kpi-sub">Approuvés : ${data.escomptesApprouves}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">Taux approbation Réf.</div>
        <div class="kpi-value">${data.tauxApprobationRefinancements} %</div>
        <div class="kpi-sub">Approuvés : ${data.refinancementsApprouves}</div>
      </div>
    </div>
  </div>

  <hr class="divider">

  <!-- ═══ 2. SYNTHÈSE FINANCIÈRE ═══ -->
  <div class="section">
    <div class="section-title">2. Synthèse Financière — Opérations Approuvées</div>
    <div class="two-col">

      <!-- Escomptes -->
      <div class="fin-card">
        <table class="table-green">
          <thead><tr>
            <th colspan="2">Escomptes (Opérations clients)</th>
          </tr></thead>
          <tbody>
            <tr><td>Montant total escompté</td><td style="text-align:right">${fmtP(data.montantTotalEscomptes)}</td></tr>
            <tr><td style="color:#dc2626">Agios bancaires (charges)</td><td style="text-align:right;color:#dc2626">− ${fmtP(data.agiosTotaux)}</td></tr>
            <tr class="fin-total fin-total-esc">
              <td style="color:#059669">✓ Net reçu par UNIMAGEC</td>
              <td style="text-align:right;color:#059669">${fmtP(data.netRecuTotal)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Refinancements -->
      <div class="fin-card">
        <table class="table-orange">
          <thead><tr>
            <th colspan="2">Refinancements (Opérations fournisseurs)</th>
          </tr></thead>
          <tbody>
            <tr><td>Montant total financé</td><td style="text-align:right">${fmtP(data.montantTotalRefinancements)}</td></tr>
            <tr><td style="color:#dc2626">Intérêts bancaires (charges)</td><td style="text-align:right;color:#dc2626">+ ${fmtP(data.interetsTotaux)}</td></tr>
            <tr class="fin-total fin-total-ref">
              <td style="color:#ea580c">⚠ Total à rembourser à la banque</td>
              <td style="text-align:right;color:#ea580c">${fmtP(data.totalRemboursement)}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <hr class="divider">

  <!-- ═══ 3. RÉPARTITION PAR STATUT ═══ -->
  <div class="section">
    <div class="section-title">3. Répartition par Statut</div>
    <div class="two-col">
      <table>
        <thead><tr><th>Statut</th><th style="text-align:center">Escomptes</th><th style="text-align:center">%</th></tr></thead>
        <tbody>
          ${[
            ['En attente', data.escomptesEnAttente, '#d97706'],
            ['Approuvés',  data.escomptesApprouves, '#059669'],
            ['Rejetés',    data.escomptesRejetes,   '#dc2626'],
            ['Annulés',    data.escomptesAnnules,   '#6b7280'],
            ['Clos',       data.escomptesClos,      '#6b7280'],
          ].map(([label, val, color]) => `
            <tr>
              <td style="color:${color}">${label}</td>
              <td style="text-align:center;font-weight:700">${val}</td>
              <td style="text-align:center;color:#9ca3af">${data.totalEscomptes > 0 ? Math.round(val / data.totalEscomptes * 100) : 0} %</td>
            </tr>`).join('')}
        </tbody>
      </table>
      <table>
        <thead><tr><th>Statut</th><th style="text-align:center">Refinancements</th><th style="text-align:center">%</th></tr></thead>
        <tbody>
          ${[
            ['En attente', data.refinancementsEnAttente, '#d97706'],
            ['Approuvés',  data.refinancementsApprouves, '#059669'],
            ['Rejetés',    data.refinancementsRejetes,   '#dc2626'],
            ['Annulés',    data.refinancementsAnnules,   '#6b7280'],
          ].map(([label, val, color]) => `
            <tr>
              <td style="color:${color}">${label}</td>
              <td style="text-align:center;font-weight:700">${val}</td>
              <td style="text-align:center;color:#9ca3af">${data.totalRefinancements > 0 ? Math.round(val / data.totalRefinancements * 100) : 0} %</td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>
  </div>

  <hr class="divider">

  <!-- ═══ 4. ANALYSE PAR BANQUE ═══ -->
  <div class="section">
    <div class="section-title">4. Analyse par Banque — Opérations Approuvées</div>
    <div class="two-col">
      <table class="table-green">
        <thead><tr>
          <th>Banque</th>
          <th style="text-align:center">Dossiers</th>
          <th style="text-align:right">Montant</th>
          <th style="text-align:right">Agios</th>
        </tr></thead>
        <tbody>${rowBanque(data.escomptesParBanque || [], 'Agios')}</tbody>
      </table>
      <table class="table-orange">
        <thead><tr>
          <th>Banque</th>
          <th style="text-align:center">Dossiers</th>
          <th style="text-align:right">Montant</th>
          <th style="text-align:right">Intérêts</th>
        </tr></thead>
        <tbody>${rowBanque(data.refinancementsParBanque || [], 'Intérêts')}</tbody>
      </table>
    </div>
  </div>

  <!-- ═══ 5. ÉVOLUTION MENSUELLE ═══ -->
  <div class="section page-break">
    <div class="section-title">5. Évolution Mensuelle — 6 Derniers Mois</div>
    <table class="month-table">
      <thead>
        <tr>
          <th>Mois</th>
          <th style="text-align:center">Esc. créés</th>
          <th style="text-align:right">Montant escomptes</th>
          <th style="text-align:center">Réf. créés</th>
          <th style="text-align:right">Montant refinancements</th>
          <th style="text-align:center">Total dossiers</th>
        </tr>
      </thead>
      <tbody>
        ${(data.evolutionMensuelle || []).map((m, i) => `
          <tr style="${i % 2 === 0 ? 'background:#fafafa' : ''}">
            <td><strong>${m.label} ${m.annee}</strong></td>
            <td style="text-align:center;color:#059669;font-weight:700">${m.countEscomptes}</td>
            <td style="text-align:right">${fmtP(m.montantEscomptes)}</td>
            <td style="text-align:center;color:#ea580c;font-weight:700">${m.countRefinancements}</td>
            <td style="text-align:right">${fmtP(m.montantRefinancements)}</td>
            <td style="text-align:center;font-weight:800">${m.countEscomptes + m.countRefinancements}</td>
          </tr>`).join('')}
      </tbody>
    </table>
  </div>

  <!-- ═══ PIED DE PAGE ═══ -->
  <div class="footer">
    <strong>UNIMAGEC</strong> — Univers du Matériel Agricole et du Commerce &nbsp;|&nbsp;
    Rapport généré le ${dateStr} &nbsp;|&nbsp;
    <span style="color:#dc2626">Document confidentiel — Usage interne uniquement</span>
  </div>

</div>
</body>
</html>`

  const w = window.open('', '_blank', 'width=920,height=750,scrollbars=yes')
  w.document.write(html)
  w.document.close()
  w.focus()
  setTimeout(() => { w.print() }, 600)
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function StatistiquesPage() {
  const [data, setData]       = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  const load = () => {
    setLoading(true)
    setError(null)
    getStatistiques()
      .then(r => setData(r.data))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  if (loading) return <LoadingSpinner text="Chargement des statistiques…" />

  if (error) return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <FiAlertCircle className="h-10 w-10 text-red-400 mb-3" />
      <p className="text-base font-semibold text-gray-700">Impossible de charger les statistiques</p>
      <p className="text-sm text-gray-400 mt-1 mb-4">Vérifiez que le serveur est démarré et réessayez.</p>
      <button onClick={load} className="btn-primary text-sm flex items-center gap-2">
        <FiRefreshCw className="h-4 w-4" /> Réessayer
      </button>
    </div>
  )

  // ─── Données graphiques ──────────────────────────────────────────────────────
  const barEvolution = (data.evolutionMensuelle || []).map(m => ({
    name:           m.label,
    escomptes:      Number(m.countEscomptes),
    refinancements: Number(m.countRefinancements),
  }))

  const barMontantsEvol = (data.evolutionMensuelle || []).map(m => ({
    name:           m.label,
    escomptes:      Number(m.montantEscomptes),
    refinancements: Number(m.montantRefinancements),
  }))

  const barEscBanque = (data.escomptesParBanque || []).map(b => ({
    name:     b.banqueNom,
    montant:  Number(b.montantTotal),
    dossiers: Number(b.count),
  }))

  const barRefBanque = (data.refinancementsParBanque || []).map(b => ({
    name:     b.banqueNom,
    montant:  Number(b.montantTotal),
    dossiers: Number(b.count),
  }))

  const totalAnomalies =
    Number(data.escomptesRejetes)  + Number(data.escomptesAnnules) +
    Number(data.refinancementsRejetes) + Number(data.refinancementsAnnules)

  return (
    <div className="space-y-6">

      {/* ── En-tête ── */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Statistiques &amp; Rapports</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Analyse de performance et évolution de l'activité financière
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <button onClick={load}
            className="btn-secondary text-sm flex items-center gap-2">
            <FiRefreshCw className="h-4 w-4" /> Actualiser
          </button>
          <button onClick={() => exportCSV(data)}
            className="btn-secondary text-sm flex items-center gap-2">
            <FiDownload className="h-4 w-4" /> Télécharger CSV
          </button>
          <button onClick={() => imprimerRapport(data)}
            className="btn-primary text-sm flex items-center gap-2">
            <FiPrinter className="h-4 w-4" /> Imprimer le rapport
          </button>
        </div>
      </div>

      {/* ── Section 1 : Taux de performance ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        <TauxCard
          label="Taux d'approbation — Escomptes"
          taux={data.tauxApprobationEscomptes}
          approuves={data.escomptesApprouves}
          total={data.totalEscomptes}
          color="emerald"
        />
        <TauxCard
          label="Taux d'approbation — Refinancements"
          taux={data.tauxApprobationRefinancements}
          approuves={data.refinancementsApprouves}
          total={data.totalRefinancements}
          color="orange"
        />
        <div className="card">
          <p className="text-sm text-gray-500 mb-2">Dossiers en anomalie</p>
          <p className="text-3xl font-black text-red-500 mb-1">{totalAnomalies}</p>
          <p className="text-xs text-gray-400">rejetés ou annulés (tous types)</p>
          <div className="mt-3 grid grid-cols-2 gap-2 text-xs">
            {[
              { label: 'Esc. rejetés',   value: data.escomptesRejetes,       color: 'text-red-500'    },
              { label: 'Esc. annulés',   value: data.escomptesAnnules,        color: 'text-orange-500' },
              { label: 'Réf. rejetés',   value: data.refinancementsRejetes,   color: 'text-red-500'    },
              { label: 'Réf. annulés',   value: data.refinancementsAnnules,   color: 'text-orange-500' },
            ].map(({ label, value, color }) => (
              <div key={label} className="bg-gray-50 rounded-lg px-2 py-1.5">
                <p className="text-gray-500">{label}</p>
                <p className={`font-bold text-sm ${color}`}>{value}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ── Section 2 : Évolution mensuelle ── */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
        {/* Nombre de dossiers par mois */}
        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">
            Dossiers créés — 6 derniers mois
          </h2>
          <p className="text-xs text-gray-400 mb-4">Nombre de dossiers ouverts par mois</p>
          <ResponsiveContainer width="100%" height={210}>
            <BarChart data={barEvolution} barSize={18} barGap={4}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip content={<TooltipCount />} />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Bar dataKey="escomptes"      name="Escomptes"      fill="#059669" radius={[4,4,0,0]} />
              <Bar dataKey="refinancements" name="Refinancements" fill="#ea580c" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Montants par mois */}
        <div className="card">
          <h2 className="text-base font-semibold text-gray-800 mb-1">
            Montants créés — 6 derniers mois
          </h2>
          <p className="text-xs text-gray-400 mb-4">Montants des dossiers ouverts par mois (MAD)</p>
          <ResponsiveContainer width="100%" height={210}>
            <BarChart data={barMontantsEvol} barSize={18} barGap={4}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 10 }} tickFormatter={fmtShort} />
              <Tooltip content={<TooltipMAD />} />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Bar dataKey="escomptes"      name="Escomptes (MAD)"      fill="#059669" radius={[4,4,0,0]} />
              <Bar dataKey="refinancements" name="Refinancements (MAD)" fill="#ea580c" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* ── Section 3 : Analyse par banque ── */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">

        {/* Escomptes par banque */}
        <div className="card space-y-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <div className="h-2 w-2 rounded-full bg-emerald-500" />
              <h2 className="text-base font-semibold text-gray-800">Escomptes par banque</h2>
            </div>
            <p className="text-xs text-gray-400">Dossiers approuvés — montants en MAD</p>
          </div>

          {barEscBanque.length === 0 ? (
            <p className="text-sm text-gray-400 py-8 text-center">Aucun escompte approuvé</p>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={160}>
                <BarChart data={barEscBanque} layout="vertical" barSize={16}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
                  <XAxis type="number" tick={{ fontSize: 10 }} tickFormatter={fmtShort} />
                  <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={90} />
                  <Tooltip content={<TooltipMAD />} />
                  <Bar dataKey="montant" name="Montant (MAD)" radius={[0,4,4,0]}>
                    {barEscBanque.map((_, i) => <Cell key={i} fill={BANK_COLORS[i % BANK_COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>

              {/* Tableau récapitulatif */}
              <div className="overflow-hidden rounded-xl border border-gray-100">
                <table className="w-full text-xs">
                  <thead className="bg-gray-50">
                    <tr>
                      {['Banque', 'Dossiers', 'Montant total', 'Agios'].map(h => (
                        <th key={h} className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {(data.escomptesParBanque || []).map((b, i) => (
                      <tr key={i} className="hover:bg-gray-50/60">
                        <td className="px-3 py-2 font-medium text-gray-800">{b.banqueNom}</td>
                        <td className="px-3 py-2 text-gray-600">{b.count}</td>
                        <td className="px-3 py-2 text-emerald-700 font-semibold">{fmt(b.montantTotal)}</td>
                        <td className="px-3 py-2 text-red-500">{fmt(b.chargesTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>

        {/* Refinancements par banque */}
        <div className="card space-y-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <div className="h-2 w-2 rounded-full bg-orange-500" />
              <h2 className="text-base font-semibold text-gray-800">Refinancements par banque</h2>
            </div>
            <p className="text-xs text-gray-400">Dossiers approuvés — montants en MAD</p>
          </div>

          {barRefBanque.length === 0 ? (
            <p className="text-sm text-gray-400 py-8 text-center">Aucun refinancement approuvé</p>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={160}>
                <BarChart data={barRefBanque} layout="vertical" barSize={16}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
                  <XAxis type="number" tick={{ fontSize: 10 }} tickFormatter={fmtShort} />
                  <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={90} />
                  <Tooltip content={<TooltipMAD />} />
                  <Bar dataKey="montant" name="Montant (MAD)" radius={[0,4,4,0]}>
                    {barRefBanque.map((_, i) => <Cell key={i} fill={BANK_COLORS[i % BANK_COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>

              {/* Tableau récapitulatif */}
              <div className="overflow-hidden rounded-xl border border-gray-100">
                <table className="w-full text-xs">
                  <thead className="bg-gray-50">
                    <tr>
                      {['Banque', 'Dossiers', 'Montant total', 'Intérêts'].map(h => (
                        <th key={h} className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {(data.refinancementsParBanque || []).map((b, i) => (
                      <tr key={i} className="hover:bg-gray-50/60">
                        <td className="px-3 py-2 font-medium text-gray-800">{b.banqueNom}</td>
                        <td className="px-3 py-2 text-gray-600">{b.count}</td>
                        <td className="px-3 py-2 text-orange-700 font-semibold">{fmt(b.montantTotal)}</td>
                        <td className="px-3 py-2 text-red-500">{fmt(b.chargesTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>

    </div>
  )
}
