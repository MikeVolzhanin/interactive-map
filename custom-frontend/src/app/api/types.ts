export interface Region {
  id: number;
  name: string;
}

export interface EducationLevel {
  id: number;
  level: string;
}

export interface Interest {
  id: number;
  name: string;
  description?: string;
}

export interface MapStatItem {
  regionId: number | string;
  regionName?: string;
  applicantsCount?: number;
  count: number;
}

export type MapStatsResponse = MapStatItem[] | { data: MapStatItem[] };

export interface UserInfo {
  firstName: string;
  middleName?: string | null;
  lastName: string;
  phoneNumber: string;
  yearOfAdmission: number;
  educationLevelId: number;
  regionId: number;
  interestIds: number[];
  profileCompleted?: boolean;
}

export interface AuthResponse {
  accessToken?: string;
  refreshToken?: string;
  token?: string;
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
  verificationCode: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  password: string;
}
