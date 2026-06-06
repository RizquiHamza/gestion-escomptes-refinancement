import { useState } from 'react'
import { useNavigate, Navigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { FiMail, FiLock, FiEye, FiEyeOff } from 'react-icons/fi'
import { motion } from 'framer-motion'
import { useAuth } from '../../context/AuthContext'

const ease = [0.25, 0.46, 0.45, 0.94]

const fadeLeft  = { hidden: { opacity: 0, x: -40 }, visible: { opacity: 1, x: 0, transition: { duration: 0.7, ease } } }
const fadeRight = { hidden: { opacity: 0, x:  40 }, visible: { opacity: 1, x: 0, transition: { duration: 0.7, ease } } }
const stagger   = { hidden: {}, visible: { transition: { staggerChildren: 0.1, delayChildren: 0.2 } } }
const fadeUp    = { hidden: { opacity: 0, y: 16 }, visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease } } }

export default function LoginPage() {
  const { login, loading, error, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [showPwd, setShowPwd] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm()

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  const onSubmit = async ({ email, motDePasse }) => {
    const ok = await login(email, motDePasse)
    if (ok) navigate('/dashboard')
  }

  return (
    <div className="min-h-screen flex">

      {/* ── Panneau gauche ── */}
      <motion.aside
        variants={fadeLeft} initial="hidden" animate="visible"
        className="hidden lg:flex lg:w-[45%] flex-col relative overflow-hidden"
        style={{
          backgroundImage:
            'url(https://images.unsplash.com/photo-1574943320219-553eb213f72d?auto=format&fit=crop&w=1400&q=85)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      >
        {/* Overlay dégradé bas → haut, vert sombre */}
        <div className="absolute inset-0" style={{
          background:
            'linear-gradient(to top, rgba(5,20,10,0.97) 0%, rgba(8,28,15,0.82) 35%, rgba(10,32,18,0.60) 65%, rgba(5,18,10,0.50) 100%)',
        }} />

        {/* ── Zone logo — haut ── */}
        <div className="relative z-10 flex flex-col items-center pt-14 px-10">

          {/* Badge catégorie */}
          <div className="mb-8 px-4 py-1.5 rounded-full text-[10px] font-semibold tracking-[0.2em] uppercase"
               style={{ background: 'rgba(116,198,157,0.15)', border: '1px solid rgba(116,198,157,0.3)', color: '#74c69d' }}>
            Système de gestion interne
          </div>

          {/* Carte logo — fond blanc pur pour lisibilité maximale */}
          <motion.div
            animate={{ y: [-5, 5, -5] }}
            transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
            className="bg-white rounded-2xl flex items-center justify-center px-10 py-7"
            style={{ boxShadow: '0 24px 60px rgba(0,0,0,0.45), 0 4px 16px rgba(0,0,0,0.25)' }}
          >
            <img
              src="/unimagec-logo.png"
              alt="UNIMAGEC"
              className="h-20 w-auto"
              onError={e => { e.target.style.display = 'none' }}
            />
          </motion.div>
        </div>

        {/* ── Zone texte — bas ── */}
        <div className="relative z-10 mt-auto px-10 pb-12">

          {/* Ligne verte accent */}
          <div className="w-10 h-1 rounded-full bg-green-400 mb-5" />

          <h2 className="text-4xl font-black text-white leading-tight mb-3"
              style={{ textShadow: '0 2px 16px rgba(0,0,0,0.6)' }}>
            UNIMAGEC
          </h2>

          <p className="text-green-300 text-sm font-medium mb-1">
            Univers du matériel agricole
          </p>
          <p className="text-white/40 text-xs mb-10">
            et du commerce
          </p>

          {/* Ligne séparatrice */}
          <div className="w-full h-px mb-6" style={{ background: 'rgba(255,255,255,0.08)' }} />

          <p className="text-white/25 text-xs tracking-wide">
            © {new Date().getFullYear()} UNIMAGEC · Tous droits réservés
          </p>
        </div>
      </motion.aside>

      {/* ── Panneau droit ── */}
      <motion.main
        variants={fadeRight} initial="hidden" animate="visible"
        className="w-full lg:w-[58%] flex items-center justify-center bg-white px-6"
      >
        <motion.div
          variants={stagger} initial="hidden" animate="visible"
          className="w-full max-w-[360px]"
        >
          {/* Logo mobile */}
          <motion.div variants={fadeUp} className="lg:hidden flex justify-center mb-8">
            <img src="/unimagec-logo.png" alt="UNIMAGEC" className="h-14 w-auto"
                 onError={e => { e.target.style.display = 'none' }} />
          </motion.div>

          {/* Titre */}
          <motion.h2 variants={fadeUp} className="text-4xl font-bold text-slate-900 mb-8">
            Connexion
          </motion.h2>

          {/* Erreur */}
          {error && (
            <motion.p
              initial={{ opacity: 0 }} animate={{ opacity: 1 }}
              className="mb-5 px-4 py-3 rounded-xl bg-red-50 border border-red-100 text-red-600 text-sm"
            >
              {error}
            </motion.p>
          )}

          {/* Formulaire */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

            {/* Email */}
            <motion.div variants={fadeUp}>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">Email</label>
              <div className="relative">
                <FiMail className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                <input
                  type="email"
                  placeholder="vous@unimagec.ma"
                  autoComplete="email"
                  className={`w-full pl-10 pr-4 py-2.5 rounded-xl text-sm border outline-none
                    transition-all duration-200 placeholder-slate-300
                    focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                    ${errors.email ? 'border-red-300' : 'border-slate-200 hover:border-slate-300'}`}
                  {...register('email', {
                    required: 'Champ requis',
                    pattern: { value: /\S+@\S+\.\S+/, message: 'Email invalide' },
                  })}
                />
              </div>
              {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
            </motion.div>

            {/* Mot de passe */}
            <motion.div variants={fadeUp}>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-sm font-medium text-slate-600">Mot de passe</label>
                <Link to="/mot-de-passe-oublie"
                  className="text-xs text-emerald-700 hover:text-emerald-800 transition-colors
                             underline underline-offset-2">
                  Mot de passe oublié ?
                </Link>
              </div>
              <div className="relative">
                <FiLock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="••••••••"
                  autoComplete="current-password"
                  className={`w-full pl-10 pr-11 py-2.5 rounded-xl text-sm border outline-none
                    transition-all duration-200 placeholder-slate-300
                    focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                    ${errors.motDePasse ? 'border-red-300' : 'border-slate-200 hover:border-slate-300'}`}
                  {...register('motDePasse', {
                    required: 'Champ requis',
                    minLength: { value: 4, message: 'Min. 4 caractères' },
                  })}
                />
                <button type="button" onClick={() => setShowPwd(v => !v)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors">
                  {showPwd ? <FiEyeOff className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
                </button>
              </div>
              {errors.motDePasse && <p className="mt-1 text-xs text-red-500">{errors.motDePasse.message}</p>}
            </motion.div>

            {/* Bouton */}
            <motion.div variants={fadeUp} className="pt-1">
              <motion.button
                type="submit"
                disabled={loading}
                whileHover={!loading ? { scale: 1.01 } : {}}
                whileTap={!loading  ? { scale: 0.99 } : {}}
                className="w-full py-3 rounded-xl text-white text-sm font-semibold
                           transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed"
                style={{
                  background: loading ? '#94a3b8' : 'linear-gradient(135deg, #164d2e 0%, #2d6a4f 100%)',
                  boxShadow: loading ? 'none' : '0 4px 14px rgba(22,77,46,0.3)',
                }}
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                      <circle cx="12" cy="12" r="10" stroke="white" strokeWidth="3"
                              strokeDasharray="30 70" strokeLinecap="round" />
                    </svg>
                    Connexion…
                  </span>
                ) : 'Se connecter'}
              </motion.button>
            </motion.div>
          </form>
        </motion.div>
      </motion.main>
    </div>
  )
}
