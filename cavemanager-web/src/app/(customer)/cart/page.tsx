'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { apiClient } from '@/lib/api-client';
import { useAuthStore, useCartStore, Commande, CartItem } from '@/lib/store';
import { Trash2, Plus, Minus } from 'lucide-react';

export default function CartPage() {
  const router = useRouter();
  const { client } = useAuthStore();
  const [cart, setCart] = useState<Commande | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dateRetrait, setDateRetrait] = useState('');
  const [notes, setNotes] = useState('');
  const [validating, setValidating] = useState(false);

  useEffect(() => {
    if (!client) {
      router.push('/login');
      return;
    }
    loadCart();
  }, [client, router]);

  const loadCart = async () => {
    try {
      setLoading(true);
      const response = await apiClient.getCart();
      if (response.success && response.data) {
        setCart(response.data);
      }
    } catch (err) {
      setError('Erreur lors du chargement du panier');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateQuantity = async (ligneId: number, quantite: number) => {
    if (quantite <= 0) {
      handleRemoveItem(ligneId);
      return;
    }

    try {
      await apiClient.updateCartLine(ligneId, quantite);
      loadCart();
    } catch (err) {
      setError('Erreur lors de la mise à jour');
      console.error(err);
    }
  };

  const handleRemoveItem = async (ligneId: number) => {
    try {
      await apiClient.removeFromCart(ligneId);
      loadCart();
    } catch (err) {
      setError('Erreur lors de la suppression');
      console.error(err);
    }
  };

  const handleValidateOrder = async () => {
    if (!dateRetrait) {
      setError('Veuillez sélectionner une date de retrait');
      return;
    }

    if (!cart || cart.lignes.length === 0) {
      setError('Votre panier est vide');
      return;
    }

    setValidating(true);
    try {
      const response = await apiClient.validateOrder(dateRetrait, notes);
      if (response.success) {
        router.push('/orders');
      } else {
        setError(response.message || 'Erreur lors de la validation');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erreur lors de la validation');
      console.error(err);
    } finally {
      setValidating(false);
    }
  };

  if (loading) return <div>Chargement du panier...</div>;

  if (!cart || cart.lignes.length === 0) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-6">Votre Panier</h1>
        <p className="text-gray-600 mb-6">Votre panier est vide</p>
        <Link href="/shop" className="btn-primary">
          Continuer les achats
        </Link>
      </div>
    );
  }

  const total = cart.lignes.reduce((sum, item) => sum + item.sousTotal, 0);

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Votre Panier</h1>

      {error && <div className="alert-error mb-6">{error}</div>}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2">
          <div className="card">
            <div className="space-y-4">
              {cart.lignes.map((item) => (
                <div key={item.id} className="flex gap-4 pb-4 border-b last:border-b-0">
                  <div className="flex-1">
                    <h3 className="font-bold">{item.produitNom}</h3>
                    <p className="text-gray-600 text-sm">
                      {item.prixUnitaire.toFixed(2)}€ x {item.quantite}
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleUpdateQuantity(item.id || 0, item.quantite - 1)}
                      className="p-1 hover:bg-gray-200 rounded"
                    >
                      <Minus size={18} />
                    </button>
                    <span className="w-8 text-center">{item.quantite}</span>
                    <button
                      onClick={() => handleUpdateQuantity(item.id || 0, item.quantite + 1)}
                      className="p-1 hover:bg-gray-200 rounded"
                    >
                      <Plus size={18} />
                    </button>
                  </div>

                  <div className="text-right">
                    <p className="font-bold">{item.sousTotal.toFixed(2)}€</p>
                    <button
                      onClick={() => handleRemoveItem(item.id || 0)}
                      className="text-red-600 hover:text-red-800 mt-2"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-4 text-right">
            <p className="text-gray-600">Sous-total: <span className="text-xl font-bold">{total.toFixed(2)}€</span></p>
            <p className="text-sm text-gray-500">TVA incluse</p>
          </div>
        </div>

        {/* Checkout Form */}
        <div className="card">
          <h2 className="text-xl font-bold mb-6">Finaliser la commande</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Date de retrait prévue *</label>
              <input
                type="datetime-local"
                value={dateRetrait}
                onChange={(e) => setDateRetrait(e.target.value)}
                className="input"
                required
              />
              <p className="text-xs text-gray-500 mt-1">Minimum 24h à partir de maintenant</p>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Notes spéciales</label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="input resize-none h-24"
                placeholder="Allergies, demandes spéciales..."
              />
            </div>

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex justify-between mb-2">
                <span>Sous-total</span>
                <span>{total.toFixed(2)}€</span>
              </div>
              <div className="flex justify-between mb-2">
                <span>Frais</span>
                <span>0.00€</span>
              </div>
              <div className="border-t pt-2 flex justify-between font-bold">
                <span>Total</span>
                <span>{total.toFixed(2)}€</span>
              </div>
              <p className="text-xs text-gray-500 mt-2">Paiement en boutique</p>
            </div>

            <button
              onClick={handleValidateOrder}
              disabled={validating}
              className="w-full btn-primary disabled:opacity-50"
            >
              {validating ? 'Validation en cours...' : 'Valider la commande'}
            </button>

            <Link href="/shop" className="block text-center text-primary hover:underline">
              Continuer les achats
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
