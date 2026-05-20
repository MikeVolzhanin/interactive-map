export interface Region {
  id: string;
  name: string;
  code: string;
}

export interface MapStatsResponse {
  data: {
    regionId: string;
    count: number;
  }[];
}

export interface UserInfo {
  firstName: string;
  lastName: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  message?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
}

export interface VerifyOtpRequest {
  email: string;
  code: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  password: string;
  confirmPassword: string;
}
