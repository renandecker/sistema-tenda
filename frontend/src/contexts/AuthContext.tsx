import React, { createContext, useContext, useState, useEffect } from 'react';
import { api, apiPdv } from '@/services/api';

interface User {
  id: string;
  nome: string;
  email: string;
  perfis: string[];
  pdvId?: string;
  empresaId?: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (email: string, senha: string, rememberMe?: boolean) => Promise<void>;
  logout: () => void;
  resetPassword: (email: string) => Promise<void>;
  hasPermission: (permission: string) => boolean;
  isInRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    if (token && userData) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setUser(JSON.parse(userData));
    }
    setLoading(false);
  }, []);

  const login = async (email: string, senha: string, rememberMeParam: boolean = false) => {
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { email, senha, rememberMe: rememberMeParam });
      const { token, user: userData, expiresIn } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      if (rememberMeParam) {
        localStorage.setItem('remember_me', 'true');
      } else {
        localStorage.removeItem('remember_me');
      }
      
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      // Set token expiration
      if (expiresIn) {
        const expiryTime = new Date().getTime() + (expiresIn * 1000);
        localStorage.setItem('token_expiry', expiryTime.toString());
      }
      
      setUser(userData);
      try {
        await apiPdv.abrirCaixaUsuario({
          operador: userData?.nome ?? 'SISTEMA',
          pdvId: userData?.pdvId,
          valorInicial: 0,
        });
      } catch {
        // caixa já criado ou erro ignorado no login
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Credenciais inválidas';
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('remember_me');
    localStorage.removeItem('token_expiry');
    delete api.defaults.headers.common['Authorization'];
    setUser(null);
  };

  const resetPassword = async (email: string): Promise<void> => {
    await api.post('/auth/reset-password', { email });
  };

  const hasPermission = (permission: string) => {
    if (!user) return false;
    return user.perfis.includes('ADMIN') || user.perfis.includes(permission);
  };

  const isInRole = (role: string) => {
    if (!user) return false;
    return user.perfis.includes(role);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loading, login, logout, resetPassword, hasPermission, isInRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}