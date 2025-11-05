export interface MenuItem {
  label?: string;
  icon?: string;
  route?: string;
  children?: MenuItem[];
  roles?: string[]; // ['ADMIN', 'USER']
  badge?: string;
  divider?: boolean;
  external?: boolean;
}

export interface User {
  id: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'USER';
  avatar?: string;
  createdAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ResetPasswordRequest {
  token: string;
  password: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ChangeEmailRequest {
  email: string;
  password: string;
}

