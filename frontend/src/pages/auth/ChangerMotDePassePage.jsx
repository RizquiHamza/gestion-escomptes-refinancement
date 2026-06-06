import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { FiLock, FiEye, FiEyeOff, FiShield } from 'react-icons/fi'
import { motion } from 'framer-motion'
import { useAuth } from '../../context/AuthContext'

const ease    = [0.25, 0.46, 0.45, 0.94]
const fadeUp  = { hidden: { opacity: 0, y: 20 }, visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease } } }
const stagger = { hidden: {}, visible: { transition: { staggerChildren: 0.08, delayChildren: 0.1 } } }

export default function ChangerMotDePassePage() {
  const { changerMotDePasse, user, logout } = useAuth()
  const navigate                            = useNavigate()
  const [loading, setLoading]               = useState(false)
  const [erreur, setErreur]                 = useState(null)
  const [showPwd, setShowPwd]               = useState(false)
  const [showConf, setShowConf]             = useState(false)

  const { register, handleSubmit, watch, formState: { errors } } = useForm()
  const nouveauMotDePasse = watch('nouveauMotDePasse')

  const onSubmit = async ({ nouveauMotDePasse: mdp }) => {
    setLoading(true)
    setErreur(null)
    try {
      await changerMotDePasse(mdp)
      navigate('/dashboard', { replace: true })
    } catch (e) {
      setErreur(e?.response?.data?.message || 'Une erreur est survenue. Réessayez.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4"
         style={{ background: 'linear-gradient(135deg, #071a0e 0%, #0c2716 60%, #071a0e 100%)' }}>

      <motion.div variants={stagger} initial="hidden" animate="visible" className="w-full max-w-sm">

        {/* Logo */}
        <motion.div variants={fadeUp} className="flex justify-center mb-8">
          <div className="bg-white rounded-2xl px-8 py-5 shadow-2xl">
            <img src="/unimagec-logo.png" alt="UNIMAGEC" className="h-12 w-auto"
                 onError={e => { e.target.style.display = 'none' }} />
          </div>
        </motion.div>

        <motion.div variants={fadeUp} className="bg-white rounded-2xl shadow-2xl p-8">

          {/* Bannière d'avertissement */}
          <div className="flex items-start gap-3 bg-amber-50 border border-amber-200
                          rounded-xl px-4 py-3 mb-6">
            <FiShield className="h-5 w-5 text-amber-600 mt-0.5 flex-shrink-0" />
            <div>
              <p className="text-sm font-semibold text-amber-800">Sécurité de votre compte</p>
              <p className="text-xs text-amber-700 mt-0.5 leading-relaxed">
                Vous utilisez un mot de passe temporaire. Vous devez en choisir
                un nouveau avant d'accéder au système.
              </p>
            </div>
          </div>

          {/* Titre + identité */}
          <div className="mb-6">
            <h1 className="text-xl font-bold text-gray-900 mb-1">Nouveau mot de passe</h1>
            <p className="text-sm text-gray-400">
              Connecté en tant que <span className="font-medium text-gray-600">{user?.prenom} {user?.nom}</span>
            </p>
          </div>

          {/* Erreur */}
          {erreur && (
            <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }}
              className="mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-100 text-red-600 text-sm">
              {erreur}
            </motion.p>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

            {/* Nouveau mot de passe */}
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1.5">
                Nouveau mot de passe
              </label>
              <div className="relative">
                <FiLock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Minimum 6 caractères"
                  autoComplete="new-password"
                  className={`w-full pl-10 pr-11 py-2.5 rounded-xl text-sm border outline-none
                    transition-all duration-200 placeholder-gray-300
                    focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                    ${errors.nouveauMotDePasse ? 'border-red-300' : 'border-gray-200 hover:border-gray-300'}`}
                  {...register('nouveauMotDePasse', {
                    required: 'Champ requis',
                    minLength: { value: 6, message: 'Minimum 6 caractères' },
                  })}
                />
                <button type="button" onClick={() => setShowPwd(v => !v)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showPwd ? <FiEyeOff className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
                </button>
              </div>
              {errors.nouveauMotDePasse && (
                <p className="mt-1 text-xs text-red-500">{errors.nouveauMotDePasse.message}</p>
              )}
            </div>

            {/* Confirmation */}
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1.5">
                Confirmer le mot de passe
              </label>
              <div className="relative">
                <FiLock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type={showConf ? 'text' : 'password'}
                  placeholder="••••••••"
                  autoComplete="new-password"
                  className={`w-full pl-10 pr-11 py-2.5 rounded-xl text-sm border outline-none
                    transition-all duration-200 placeholder-gray-300
                    focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                    ${errors.confirmation ? 'border-red-300' : 'border-gray-200 hover:border-gray-300'}`}
                  {...register('confirmation', {
                    required: 'Champ requis',
                    validate: v => v === nouveauMotDePasse || 'Les mots de passe ne correspondent pas',
                  })}
                />
                <button type="button" onClick={() => setShowConf(v => !v)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showConf ? <FiEyeOff className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
                </button>
              </div>
              {errors.confirmation && (
                <p className="mt-1 text-xs text-red-500">{errors.confirmation.message}</p>
              )}
            </div>

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
                  Enregistrement…
                </span>
              ) : 'Enregistrer et accéder au système'}
            </motion.button>
          </form>

          {/* Déconnexion */}
          <div className="mt-5 text-center">
            <button onClick={logout}
              className="text-xs text-gray-400 hover:text-gray-600 underline underline-offset-2 transition-colors">
              Se déconnecter
            </button>
          </div>
        </motion.div>

        <motion.p variants={fadeUp} className="text-center text-white/20 text-xs mt-6">
          © {new Date().getFullYear()} UNIMAGEC · Tous droits réservés
        </motion.p>
      </motion.div>
    </div>
  )
}
