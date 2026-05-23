import { useState } from 'react'
import { useNavigate, Navigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { FiMail, FiLock, FiEye, FiEyeOff, FiBarChart2 } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'

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
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-blue-950 to-gray-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center h-14 w-14 rounded-2xl bg-blue-600 mb-4">
            <FiBarChart2 className="h-7 w-7 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white">Gestion Financement</h1>
          <p className="text-gray-400 mt-1 text-sm">Connectez-vous à votre espace</p>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
              <div className="relative">
                <FiMail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="email"
                  placeholder="votre@email.com"
                  className={`input-field pl-10 ${errors.email ? 'border-red-400' : ''}`}
                  {...register('email', {
                    required: 'Email requis',
                    pattern: { value: /\S+@\S+\.\S+/, message: 'Email invalide' },
                  })}
                />
              </div>
              {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Mot de passe</label>
              <div className="relative">
                <FiLock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="••••••••"
                  className={`input-field pl-10 pr-10 ${errors.motDePasse ? 'border-red-400' : ''}`}
                  {...register('motDePasse', { required: 'Mot de passe requis', minLength: { value: 4, message: 'Minimum 4 caractères' } })}
                />
                <button type="button" onClick={() => setShowPwd(!showPwd)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showPwd ? <FiEyeOff className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
                </button>
              </div>
              {errors.motDePasse && <p className="mt-1 text-xs text-red-600">{errors.motDePasse.message}</p>}
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 text-sm mt-2">
              {loading ? 'Connexion en cours…' : 'Se connecter'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
