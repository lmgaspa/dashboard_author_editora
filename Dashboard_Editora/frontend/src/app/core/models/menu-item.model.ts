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
  avatar?: string; // Deprecated - usar profilePhotoUrl
  profilePhotoUrl?: string | null;
  createdAt?: string;
  authorId?: string;
  ecommerceUrl?: string;
  ecommerceDbUrl?: string;
  ecommerceDbUsername?: string;
  ecommerceDbPassword?: string | null;
}

export interface AuthResponse {
  accessToken: string;
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
  newEmail: string;
}

export interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  authProvider: string;
  passwordSet: boolean;
  profilePhotoUrl?: string | null;
}

