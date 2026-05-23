import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Navbar from './Navbar'

const PAGE_TITLES = {
  '/dashboard':      'Dashboard',
  '/escomptes':      'Escomptes',
  '/refinancements': 'Refinancements',
  '/partenaires':    'Partenaires',
  '/banques':        'Banques',
  '/utilisateurs':   'Utilisateurs',
  '/logs':           'Logs d\'activité',
}

export default function MainLayout() {
  const { pathname } = useLocation()
  const title = PAGE_TITLES[pathname] || 'Gestion Financement'

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar title={title} />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
