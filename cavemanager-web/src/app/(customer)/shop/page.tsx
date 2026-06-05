'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { apiClient } from '@/lib/api-client';
import { useCartStore, useAuthStore } from '@/lib/store';
import { ShoppingCart, AlertTriangle } from 'lucide-react';

interface Produit {
  id: number;
  nom: string;
  appellation?: string;
  millesime?: number;
  producteur?: string;
  prixVente: number;
  quantiteStock: number;
  estEnAlerte: boolean;
  description?: string;
}

export default function ShopPage() {
  const [produits, setProduits] = useState<Produit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [selectedQuantities, setSelectedQuantities] = useState<Record<number, number>>({});
  const { client } = useAuthStore();

  useEffect(() => {
    loadProducts();
  }, [search]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const response = await apiClient.getProducts(1, 100, search);
      if (response.success && response.data) {
        setProduits(response.data);
      }
    } catch (err) {
      setError('Erreur lors du chargement des produits');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async (produit: Produit) => {
    if (!client) {
      // Redirect to login
      window.location.href = '/login';
      return;
    }

    const quantite = selectedQuantities[produit.id] || 1;
    try {
      const response = await apiClient.addToCart(produit.id, quantite);
      if (response.success) {
        // Show success message
        alert('Produit ajouté au panier');
        setSelectedQuantities({ ...selectedQuantities, [produit.id]: 1 });
      }
    } catch (err) {
      setError('Erreur lors de l\'ajout au panier');
      console.error(err);
    }
  };

  return (
    <div>
      <h1 className="text-4xl font-bold mb-8">Notre Sélection de Vins</h1>

      {/* Search */}
      <div className="mb-8">
        <input
          type="text"
          placeholder="Rechercher un vin..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="input w-full max-w-md"
        />
      </div>

      {/* Error */}
      {error && <div className="alert-error">{error}</div>}

      {/* Loading */}
      {loading && <div>Chargement...</div>}

      {/* Products Grid */}
      {!loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {produits.map((produit) => (
            <div key={produit.id} className="card hover:shadow-lg transition-shadow">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-bold text-lg">{produit.nom}</h3>
                  {produit.appellation && (
                    <p className="text-sm text-gray-600">{produit.appellation}</p>
                  )}
                </div>
                {produit.estEnAlerte && (
                  <div className="flex items-center gap-1 text-orange-600">
                    <AlertTriangle size={16} />
                    <span className="text-xs">Stock limité</span>
                  </div>
                )}
              </div>

              <div className="text-gray-700 mb-4">
                {produit.producteur && (
                  <p className="text-sm">{produit.producteur}</p>
                )}
                {produit.millesime && (
                  <p className="text-sm">Millésime: {produit.millesime}</p>
                )}
                {produit.description && (
                  <p className="text-sm mt-2">{produit.description}</p>
                )}
              </div>

              <div className="flex justify-between items-center mb-4">
                <span className="text-2xl font-bold text-primary">
                  {produit.prixVente.toFixed(2)}€
                </span>
                <span className={`text-sm ${
                  produit.quantiteStock > 0 ? 'text-green-600' : 'text-red-600'
                }`}>
                  {produit.quantiteStock > 0 ? `${produit.quantiteStock} en stock` : 'Rupture'}
                </span>
              </div>

              <div className="flex gap-2">
                <input
                  type="number"
                  min="1"
                  max={produit.quantiteStock}
                  value={selectedQuantities[produit.id] || 1}
                  onChange={(e) =>
                    setSelectedQuantities({
                      ...selectedQuantities,
                      [produit.id]: Math.min(
                        parseInt(e.target.value) || 1,
                        produit.quantiteStock
                      ),
                    })
                  }
                  className="input w-16"
                  disabled={produit.quantiteStock === 0}
                />
                <button
                  onClick={() => handleAddToCart(produit)}
                  disabled={produit.quantiteStock === 0}
                  className={`flex-1 flex items-center justify-center gap-2 rounded-lg px-4 py-2 transition-colors ${
                    produit.quantiteStock === 0
                      ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                      : 'btn-primary'
                  }`}
                >
                  <ShoppingCart size={18} />
                  Ajouter
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && produits.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-600 mb-4">Aucun produit trouvé</p>
          {search && (
            <button
              onClick={() => setSearch('')}
              className="btn-outline"
            >
              Réinitialiser la recherche
            </button>
          )}
        </div>
      )}
    </div>
  );
}
