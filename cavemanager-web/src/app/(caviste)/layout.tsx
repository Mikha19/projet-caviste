'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Menu, X, LogOut, BarChart3, Package, ShoppingCart } from 'lucide-react';
import { useState } from 'react';

export default function CavisteLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const router = useRouter();

  // TODO: Implement caviste authentication check
  const handleLogout = () => {
    router.push('/');
  };

  return (
    <div className="min-h-screen flex bg-gray-50">
      {/* Sidebar */}
      <aside
        className={`${
          sidebarOpen ? 'w-64' : 'w-20'
        } bg-primary text-white transition-all duration-300 shadow-lg`}
      >
        <div className="p-4 flex items-center justify-between border-b border-red-900">
          <Link href="/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-secondary rounded flex items-center justify-center text-primary font-bold">
              🍷
            </div>
            {sidebarOpen && <span className="font-bold">CaveManager</span>}
          </Link>
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="hover:bg-red-900 p-1 rounded"
          >
            {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-2">
          <Link
            href="/dashboard"
            className="flex items-center gap-3 p-2 rounded hover:bg-red-900 transition"
          >
            <BarChart3 size={20} />
            {sidebarOpen && <span>Tableau de bord</span>}
          </Link>

          <Link
            href="/stock"
            className="flex items-center gap-3 p-2 rounded hover:bg-red-900 transition"
          >
            <Package size={20} />
            {sidebarOpen && <span>Gestion du stock</span>}
          </Link>

          <Link
            href="/orders"
            className="flex items-center gap-3 p-2 rounded hover:bg-red-900 transition"
          >
            <ShoppingCart size={20} />
            {sidebarOpen && <span>Commandes</span>}
          </Link>
        </nav>

        <div className="absolute bottom-0 left-0 right-0 border-t border-red-900">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 p-4 hover:bg-red-900 transition"
          >
            <LogOut size={20} />
            {sidebarOpen && <span>Déconnexion</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="bg-white shadow-sm border-b p-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">Interface Caviste</h1>
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600">
                Connecté en tant que caviste
              </span>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-auto p-6">{children}</main>
      </div>
    </div>
  );
}
