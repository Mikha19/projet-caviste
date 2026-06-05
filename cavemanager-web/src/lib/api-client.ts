import axios, { AxiosInstance, AxiosError } from 'axios';

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: string[];
}

export class ApiClient {
  private client: AxiosInstance;
  private baseURL: string;

  constructor(baseURL: string = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080') {
    this.baseURL = baseURL;
    this.client = axios.create({
      baseURL: `${baseURL}/api/v1`,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests if available
    this.client.interceptors.request.use((config) => {
      const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle responses
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Clear token and redirect to login
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token');
            localStorage.removeItem('client');
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  // Authentication
  async register(data: {
    email: string;
    password: string;
    nom: string;
    prenom: string;
    telephone?: string;
    adresse?: string;
    codePostal?: string;
    ville?: string;
  }): Promise<ApiResponse<any>> {
    const response = await this.client.post('/auth/register', data);
    return response.data;
  }

  async login(email: string, password: string): Promise<ApiResponse<any>> {
    const response = await this.client.post('/auth/login', {
      email,
      password,
    });
    return response.data;
  }

  // Products
  async getProducts(page: number = 1, limit: number = 20, search?: string): Promise<ApiResponse<any>> {
    const response = await this.client.get('/produits', {
      params: { page, limit, search },
    });
    return response.data;
  }

  async getProduct(id: number): Promise<ApiResponse<any>> {
    const response = await this.client.get(`/produits/${id}`);
    return response.data;
  }

  async getLowStockAlerts(): Promise<ApiResponse<any>> {
    const response = await this.client.get('/produits/alerts/stock');
    return response.data;
  }

  // Orders
  async getOrders(): Promise<ApiResponse<any>> {
    const response = await this.client.get('/commandes');
    return response.data;
  }

  async getCart(): Promise<ApiResponse<any>> {
    const response = await this.client.get('/commandes/panier');
    return response.data;
  }

  async addToCart(produitId: number, quantite: number): Promise<ApiResponse<any>> {
    const response = await this.client.post('/commandes/panier/add', {
      produitId,
      quantite,
    });
    return response.data;
  }

  async updateCartLine(ligneId: number, quantite: number): Promise<ApiResponse<any>> {
    const response = await this.client.put(`/commandes/panier/ligne/${ligneId}`, {
      quantite,
    });
    return response.data;
  }

  async removeFromCart(ligneId: number): Promise<ApiResponse<any>> {
    const response = await this.client.delete(`/commandes/panier/ligne/${ligneId}`);
    return response.data;
  }

  async validateOrder(dateRetraitPrevue: string, notes?: string): Promise<ApiResponse<any>> {
    const response = await this.client.post('/commandes/panier/valider', {
      dateRetraitPrevue,
      notes,
    });
    return response.data;
  }

  async getOrder(id: number): Promise<ApiResponse<any>> {
    const response = await this.client.get(`/commandes/${id}`);
    return response.data;
  }

  // Profile
  async getProfile(): Promise<ApiResponse<any>> {
    const response = await this.client.get('/profile');
    return response.data;
  }

  async updateProfile(data: {
    nom?: string;
    prenom?: string;
    telephone?: string;
    adresse?: string;
    codePostal?: string;
    ville?: string;
  }): Promise<ApiResponse<any>> {
    const response = await this.client.put('/profile', data);
    return response.data;
  }
}

export const apiClient = new ApiClient();
