import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface Client {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  telephone?: string;
  adresse?: string;
  codePostal?: string;
  ville?: string;
}

export interface CartItem {
  id?: number;
  produitId: number;
  produitNom?: string;
  quantite: number;
  prixUnitaire: number;
  sousTotal: number;
}

export interface Commande {
  id: number;
  numerCommande: string;
  statut: 'PANIER' | 'VALIDEE' | 'EN_PREPARATION' | 'PRETE' | 'RETIREE' | 'ANNULEE';
  montantTotal: number;
  montantPaye: number;
  lignes: CartItem[];
  dateCreation: string;
  dateRetraitPrevue?: string;
}

interface AuthStore {
  client: Client | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
  setClient: (client: Client | null) => void;
  setToken: (token: string | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  logout: () => void;
}

interface CartStore {
  cart: Commande | null;
  isLoading: boolean;
  error: string | null;
  setCart: (cart: Commande | null) => void;
  addItem: (item: CartItem) => void;
  updateItem: (ligneId: number, quantite: number) => void;
  removeItem: (ligneId: number) => void;
  clearCart: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      client: null,
      token: null,
      isLoading: false,
      error: null,
      setClient: (client) => set({ client }),
      setToken: (token) => set({ token }),
      setLoading: (isLoading) => set({ isLoading }),
      setError: (error) => set({ error }),
      logout: () => set({ client: null, token: null }),
    }),
    {
      name: 'auth-store',
    }
  )
);

export const useCartStore = create<CartStore>()(
  persist(
    (set) => ({
      cart: null,
      isLoading: false,
      error: null,
      setCart: (cart) => set({ cart }),
      addItem: (item) =>
        set((state) => {
          if (!state.cart) return state;
          const existingItem = state.cart.lignes.find((i) => i.produitId === item.produitId);
          if (existingItem && existingItem.id) {
            return {
              cart: {
                ...state.cart,
                lignes: state.cart.lignes.map((i) =>
                  i.id === existingItem.id
                    ? { ...i, quantite: i.quantite + item.quantite }
                    : i
                ),
              },
            };
          }
          return {
            cart: {
              ...state.cart,
              lignes: [...state.cart.lignes, item],
            },
          };
        }),
      updateItem: (ligneId, quantite) =>
        set((state) => {
          if (!state.cart) return state;
          return {
            cart: {
              ...state.cart,
              lignes: state.cart.lignes.map((i) =>
                i.id === ligneId ? { ...i, quantite } : i
              ),
            },
          };
        }),
      removeItem: (ligneId) =>
        set((state) => {
          if (!state.cart) return state;
          return {
            cart: {
              ...state.cart,
              lignes: state.cart.lignes.filter((i) => i.id !== ligneId),
            },
          };
        }),
      clearCart: () => set({ cart: null }),
      setLoading: (isLoading) => set({ isLoading }),
      setError: (error) => set({ error }),
    }),
    {
      name: 'cart-store',
    }
  )
);
