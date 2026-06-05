'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store';
import { ShoppingCart, LogOut, User, Menu, X } from 'lucide-react';
import { useState } from 'react';

export default function CustomerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { client, logout } = useAuthStore();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-primary text-white shadow-lg">
        <div className="container flex justify-between items-center py-4">
          <Link href="/shop" className="text-2xl font-bold flex items-center gap-2">
            <div className="w-8 h-8 bg-secondary rounded flex items-center justify-center">
              🍷
            </div>
            CaveManager
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-6">
            <Link href="/shop" className="hover:text-secondary transition">
              Boutique
            </Link>
            <Link href="/orders" className="hover:text-secondary transition">
              Mes Commandes
            </Link>
            {client && (
              <>
                <Link href="/cart" className="flex items-center gap-1 hover:text-secondary transition">
                  <ShoppingCart size={20} />
                  Panier
                </Link>
                <Link href="/profile" className="flex items-center gap-1 hover:text-secondary transition">
                  <User size={20} />
                  {client.prenom}
                </Link>
                <button onClick={handleLogout} className="flex items-center gap-1 hover:text-secondary transition">
                  <LogOut size={20} />
                  Déconnexion
                </button>
              </>
            )}
            {!client && (
              <>
                <Link href="/login" className="hover:text-secondary transition">
                  Connexion
                </Link>
                <Link href="/register" className="bg-secondary text-primary px-4 py-2 rounded hover:bg-yellow-600 transition">
                  S'inscrire
                </Link>
              </>
            )}
          </nav>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile Navigation */}
        {mobileMenuOpen && (
          <nav className="md:hidden bg-red-900 p-4 flex flex-col gap-4">
            <Link href="/shop" onClick={() => setMobileMenuOpen(false)}>
              Boutique
            </Link>
            <Link href="/orders" onClick={() => setMobileMenuOpen(false)}>
              Mes Commandes
            </Link>
            {client && (
              <>
                <Link href="/cart" onClick={() => setMobileMenuOpen(false)}>
                  Panier
                </Link>
                <Link href="/profile" onClick={() => setMobileMenuOpen(false)}>
                  Mon Profil
                </Link>
                <button onClick={handleLogout}>Déconnexion</button>
              </>
            )}
            {!client && (
              <>
                <Link href="/login" onClick={() => setMobileMenuOpen(false)}>
                  Connexion
                </Link>
                <Link href="/register" onClick={() => setMobileMenuOpen(false)}>
                  S'inscrire
                </Link>
              </>
            )}
          </nav>
        )}
      </header>

      {/* Main Content */}
      <main className="flex-1 container py-8">{children}</main>

      {/* Footer */}
      <footer className="bg-gray-900 text-white mt-12">
        <div className="container py-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
            <div>
              <h3 className="font-bold text-lg mb-4">CaveManager</h3>
              <p className="text-gray-400">Votre caviste en ligne</p>
            </div>
            <div>
              <h4 className="font-bold mb-4">Navigation</h4>
              <ul className="space-y-2 text-gray-400">
                <li><Link href="/shop" className="hover:text-white">Boutique</Link></li>
                <li><Link href="/orders" className="hover:text-white">Mes Commandes</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Compte</h4>
              <ul className="space-y-2 text-gray-400">
                <li><Link href="/profile" className="hover:text-white">Mon Profil</Link></li>
                <li><Link href="/login" className="hover:text-white">Connexion</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Contact</h4>
              <p className="text-gray-400">Email: contact@cavemanager.fr</p>
              <p className="text-gray-400">Téléphone: +33 1 234 567 890</p>
            </div>
          </div>
          <div className="border-t border-gray-800 pt-8 text-center text-gray-400">
            <p>&copy; 2024 CaveManager. Tous droits réservés.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
