'use client';

import React from 'react';
import { AlertTriangle, TrendingUp, Package, Clock } from 'lucide-react';

export default function CavisteDashboard() {
  // TODO: Implement real data loading
  const stats = {
    stockAlerts: 5,
    pendingOrders: 12,
    readyForPickup: 3,
    totalRevenue: 2450.00,
  };

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Tableau de Bord</h1>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Alertes Stock</p>
              <p className="text-3xl font-bold text-orange-600">{stats.stockAlerts}</p>
            </div>
            <AlertTriangle size={32} className="text-orange-600" />
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Commandes en attente</p>
              <p className="text-3xl font-bold text-blue-600">{stats.pendingOrders}</p>
            </div>
            <Clock size={32} className="text-blue-600" />
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Prêtes pour retrait</p>
              <p className="text-3xl font-bold text-green-600">{stats.readyForPickup}</p>
            </div>
            <Package size={32} className="text-green-600" />
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Chiffre d'affaires</p>
              <p className="text-3xl font-bold text-primary">{stats.totalRevenue.toFixed(2)}€</p>
            </div>
            <TrendingUp size={32} className="text-primary" />
          </div>
        </div>
      </div>

      {/* Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Low Stock Alerts */}
        <div className="card">
          <h2 className="text-xl font-bold mb-4">Alertes de stock</h2>
          <div className="text-gray-600">
            <p className="mb-4">Les produits sous le seuil d'alerte apparaissent ici.</p>
            {/* TODO: List low stock products */}
            <button className="text-primary hover:underline">Voir tous les alertes →</button>
          </div>
        </div>

        {/* Recent Orders */}
        <div className="card">
          <h2 className="text-xl font-bold mb-4">Commandes récentes</h2>
          <div className="text-gray-600">
            <p className="mb-4">Les dernières commandes à traiter apparaissent ici.</p>
            {/* TODO: List recent orders */}
            <button className="text-primary hover:underline">Voir toutes les commandes →</button>
          </div>
        </div>
      </div>

      {/* Info Box */}
      <div className="alert-info mt-8">
        <p className="font-medium">💡 Interface caviste en développement</p>
        <p className="text-sm mt-2">
          Utilisez cette interface pour gérer le stock, traiter les commandes et suivre les alertes d'inventaire.
        </p>
      </div>
    </div>
  );
}
