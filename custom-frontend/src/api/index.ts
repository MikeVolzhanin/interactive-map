import { apiClient } from './client';
import type {
  Region,
  MapStatsResponse,
  UserInfo,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  VerifyOtpRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
} from './types';

export const api = {
  // Auth
  login: (data: LoginRequest) => 
    apiClient.post<AuthResponse>('/auth/login', data),
  
  register: (data: RegisterRequest) => 
    apiClient.post<AuthResponse>('/auth/register', data),
  
  verifyOtp: (data: VerifyOtpRequest) => 
    apiClient.post<AuthResponse>('/auth/verify-otp', data),
  
  forgotPassword: (data: ForgotPasswordRequest) => 
    apiClient.post<{ message: string }>('/auth/forgot-password', data),
  
  resetPassword: (data: ResetPasswordRequest) => 
    apiClient.post<{ message: string }>('/auth/reset-password', data),

  // Regions
  getRegions: () => 
    apiClient.get<Region[]>('/regions'),

  // Map Stats
  getMapStats: (params: { dateFrom?: string; dateTo?: string }) => {
    const queryParams = new URLSearchParams();
    if (params.dateFrom) queryParams.set('dateFrom', params.dateFrom);
    if (params.dateTo) queryParams.set('dateTo', params.dateTo);
    const query = queryParams.toString();
    return apiClient.get<MapStatsResponse>(`/map-stats/registrations-by-region${query ? `?${query}` : ''}`);
  },

  // Users
  addUserInfo: (data: UserInfo) => 
    apiClient.post<{ message: string }>('/users/add-info', data),
  
  getUserInfo: () => 
    apiClient.get<UserInfo>('/users/get-info'),
};

export * from './types';
