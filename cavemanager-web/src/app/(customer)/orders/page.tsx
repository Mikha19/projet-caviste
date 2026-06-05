'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { apiClient } from '@/lib/api-client';
import { useAuthStore } from '@/lib/store';
import { format } from 'date-fns';
import { ChevronRight, Clock, CheckCircle, Package } from 'lucide-react';

interface Commande {
  id: number;
  numerCommande: string;
  statut: string;
  montantTotal: number;
  dateCreation: string;
  dateRetraitPrevue?: string;
  dateRetraitEffective?: string;
}

interface StatusLabel {
  label: string;
  color: string;
  icon: React.ReactNode;
}

const statusLabels = {
  PANIER: { label: 'Panier', color: 'bg-gray-100 text-gray-800', icon: <Package size={18} /> },
  VALIDEE: { label: 'Validée', color: 'bg-blue-100 text-blue-800', icon: <CheckCircle size={18} /> },
  EN_PREPARATION: { label: 'En préparation', color: 'bg-yellow-100 text-yellow-800', icon: <Clock size={18} /> },
  PRETE: { label: 'Prête', color: 'bg-green-100 text-green-800', icon: <CheckCircle size={18} /> },
  RETIREE: { label: 'Retirée', color: 'bg-green-100 text-green-800', icon: <CheckCircle size={18} /> },
  ANNULEE: { label: 'Annulée', color: 'bg-red-100 text-red-800', icon: <CheckCircle size={18} /> },
} as const;

const getStatusLabel = (statut: string): StatusLabel => {
  return (statusLabels[statut as keyof typeof statusLabels] ?? statusLabels.VALIDEE) as StatusLabel;
};

export default function OrdersPage() {
  const router = useRouter();
  const { client } = useAuthStore();
  const [orders, setOrders] = useState<Commande[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!client) {
      router.push('/login');
      return;
    }
    loadOrders();
  }, [client, router]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const response = await apiClient.getOrders();
      if (response.success && response.data) {
        setOrders(response.data);
      }
    } catch (err) {
      setError('Erreur lors du chargement des commandes');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Chargement des commandes...</div>;

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Mes Commandes</h1>

      {error && <div className="alert-error mb-6">{error}</div>}

      {orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 mb-6">Vous n'avez pas encore de commandes</p>
          <Link href="/shop" className="btn-primary">
            Commencer vos achats
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders
            .filter((order) => order.statut !== 'PANIER') // Hide cart-like orders
            .map((order) => {
              const status = getStatusLabel(order.statut);
              return (
                <Link key={order.id} href={`/orders/${order.id}`}>
                  <div className="card hover:shadow-lg transition-shadow cursor-pointer">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-4 mb-2">
                          <span className="font-mono text-sm bg-gray-100 px-3 py-1 rounded">
                            {order.numerCommande}
                          </span>
                          <div className={`flex items-center gap-1 px-3 py-1 rounded ${status.color}`}>
                            {status.icon}
                            <span>{status.label}</span>
                          </div>
                        </div>
                        <div className="text-sm text-gray-600">
                          <p>Commandée le {format(new Date(order.dateCreation), 'dd/MM/yyyy à HH:mm')}</p>
                          {order.dateRetraitPrevue && (
                            <p>
                              Retrait prévu le{' '}
                              {format(new Date(order.dateRetraitPrevue), 'dd/MM/yyyy à HH:mm')}
                            </p>
                          )}
                        </div>
                      </div>

                      <div className="text-right">
                        <p className="text-2xl font-bold text-primary">
                          {order.montantTotal.toFixed(2)}€
                        </p>
                        <ChevronRight className="mt-2 text-gray-400" />
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })}
        </div>
      )}
    </div>
  );
}
