import { apiClient } from './client';
import type {
  Region,
  EducationLevel,
  Interest,
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
    apiClient.post<AuthResponse>('/auth/signup', { email: data.email, password: data.password }),
  
  verifyOtp: (data: VerifyOtpRequest) => 
    apiClient.post<AuthResponse>('/auth/verify', data),

  resendVerificationCode: (email: string) =>
    apiClient.post<{ message: string }>(`/auth/resend?email=${encodeURIComponent(email)}`),
  
  forgotPassword: (data: ForgotPasswordRequest) =>
    apiClient.post<{ message: string }>(
      `/auth/forgot-password?email=${encodeURIComponent(data.email)}`,
    ),
  
  resetPassword: (data: ResetPasswordRequest) => 
    apiClient.post<{ message: string }>('/auth/reset-password', data),

  // Regions / карта — без JWT (и без редиректа на логин при «битом» токене в localStorage)
  getRegions: () => apiClient.get<Region[]>('/map/region-catalog', { skipAuth: true }),

  getEducationLevels: () =>
    apiClient.get<EducationLevel[]>('/education-levels'),

  getInterests: () =>
    apiClient.get<Interest[]>('/interests'),

  // Map Stats
  getMapStats: (params: { dateFrom?: string; dateTo?: string }) => {
    const queryParams = new URLSearchParams();
    if (params.dateFrom) queryParams.set('dateFrom', params.dateFrom);
    if (params.dateTo) queryParams.set('dateTo', params.dateTo);
    const query = queryParams.toString();
    return apiClient.get<MapStatsResponse>(`/map/regions${query ? `?${query}` : ''}`, { skipAuth: true });
  },

  // Users
  addUserInfo: (data: UserInfo) =>
    apiClient.post<{ message: string }>('/users/add-info', data),
  
  getUserInfo: () => 
    apiClient.get<UserInfo>('/users/get-info'),
};

export * from './types';
