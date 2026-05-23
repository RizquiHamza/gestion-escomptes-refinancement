import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './routes/PrivateRoute'
import MainLayout from './layouts/MainLayout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import EscomptesPage from './pages/escomptes/EscomptesPage'
import RefinancementsPage from './pages/refinancements/RefinancementsPage'
import PartenairesPage from './pages/partenaires/PartenairesPage'
import BanquesPage from './pages/banques/BanquesPage'
import UtilisateursPage from './pages/utilisateurs/UtilisateursPage'
import LogsPage from './pages/logs/LogsPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" toastOptions={{ duration: 3500 }} />
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<PrivateRoute />}>
            <Route element={<MainLayout />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard"      element={<DashboardPage />} />
              <Route path="/escomptes"      element={<EscomptesPage />} />
              <Route path="/refinancements" element={<RefinancementsPage />} />
              <Route path="/partenaires"    element={<PartenairesPage />} />
              <Route path="/banques"        element={<BanquesPage />} />

              <Route element={<PrivateRoute roles={['ADMIN', 'RESPONSABLE']} />}>
                <Route path="/logs" element={<LogsPage />} />
              </Route>

              <Route element={<PrivateRoute roles={['ADMIN']} />}>
                <Route path="/utilisateurs" element={<UtilisateursPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
