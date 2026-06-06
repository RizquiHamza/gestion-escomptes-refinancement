import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { FiMail, FiArrowLeft, FiCheckCircle, FiMessageSquare } from 'react-icons/fi'
import { motion } from 'framer-motion'
import { demanderReinitialisation } from '../../services/authService'

const ease    = [0.25, 0.46, 0.45, 0.94]
const fadeUp  = { hidden: { opacity: 0, y: 20 }, visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease } } }
const stagger = { hidden: {}, visible: { transition: { staggerChildren: 0.08, delayChildren: 0.1 } } }

export default function MotDePasseOubliePage() {
  const [envoye, setEnvoye]   = useState(false)
  const [loading, setLoading] = useState(false)
  const { register, handleSubmit, formState: { errors }, getValues } = useForm()

  const onSubmit = async ({ email, message }) => {
    setLoading(true)
    try {
      await demanderReinitialisation(email, message || null)
    } catch {
      // On affiche toujours la confirmation (pas de révélation d'email)
    } finally {
      setLoading(false)
      setEnvoye(true)
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

          {!envoye ? (
            <>
              <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-900 mb-2">Mot de passe oublié ?</h1>
                <p className="text-sm text-gray-500 leading-relaxed">
                  Soumettez une demande à l'administrateur. Il vous communiquera
                  un mot de passe temporaire pour vous reconnecter.
                </p>
              </div>

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

                {/* Email */}
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1.5">
                    Votre adresse email
                  </label>
                  <div className="relative">
                    <FiMail className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <input
                      type="email"
                      placeholder="vous@unimagec.ma"
                      autoComplete="email"
                      className={`w-full pl-10 pr-4 py-2.5 rounded-xl text-sm border outline-none
                        transition-all duration-200 placeholder-gray-300
                        focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                        ${errors.email ? 'border-red-300' : 'border-gray-200 hover:border-gray-300'}`}
                      {...register('email', {
                        required: 'L\'email est requis',
                        pattern: { value: /\S+@\S+\.\S+/, message: 'Format invalide' },
                      })}
                    />
                  </div>
                  {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
                </div>

                {/* Message optionnel */}
                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1.5">
                    Message pour l'administrateur
                    <span className="text-gray-400 font-normal ml-1">(optionnel)</span>
                  </label>
                  <div className="relative">
                    <FiMessageSquare className="absolute left-3.5 top-3 h-4 w-4 text-gray-400" />
                    <textarea
                      rows={3}
                      placeholder="Expliquez brièvement la situation…"
                      className="w-full pl-10 pr-4 py-2.5 rounded-xl text-sm border outline-none resize-none
                        transition-all duration-200 placeholder-gray-300
                        focus:ring-2 focus:ring-green-600/20 focus:border-green-600
                        border-gray-200 hover:border-gray-300"
                      {...register('message')}
                    />
                  </div>
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
                      Envoi en cours…
                    </span>
                  ) : 'Envoyer la demande'}
                </motion.button>
              </form>
            </>
          ) : (
            /* Confirmation */
            <div className="text-center py-2">
              <div className="flex justify-center mb-4">
                <div className="h-14 w-14 rounded-full bg-emerald-50 flex items-center justify-center">
                  <FiCheckCircle className="h-7 w-7 text-emerald-600" />
                </div>
              </div>
              <h2 className="text-lg font-bold text-gray-900 mb-3">Demande envoyée</h2>
              <p className="text-sm text-gray-500 leading-relaxed mb-4">
                Votre demande a bien été transmise à l'administrateur.
                Il vous communiquera un <strong>mot de passe temporaire</strong> pour vous reconnecter.
              </p>
              <p className="text-xs text-gray-400 mb-6">
                Lors de votre prochaine connexion avec ce mot de passe temporaire,
                vous serez invité à en choisir un nouveau.
              </p>
              <button
                onClick={() => setEnvoye(false)}
                className="text-sm text-emerald-700 hover:text-emerald-800 underline underline-offset-2"
              >
                Faire une nouvelle demande
              </button>
            </div>
          )}

          {/* Retour */}
          <div className="mt-6 pt-5 border-t border-gray-100 text-center">
            <Link to="/login"
              className="inline-flex items-center gap-1.5 text-sm text-gray-500
                         hover:text-gray-700 transition-colors">
              <FiArrowLeft className="h-3.5 w-3.5" />
              Retour à la connexion
            </Link>
          </div>
        </motion.div>

        <motion.p variants={fadeUp} className="text-center text-white/20 text-xs mt-6">
          © {new Date().getFullYear()} UNIMAGEC · Tous droits réservés
        </motion.p>
      </motion.div>
    </div>
  )
}
