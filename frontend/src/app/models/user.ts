export interface User {
  id: number;
  name: string;
  email: string;
}

/** What the server sends back after login or sign-up */
export interface AuthResponse {
  token: string;
  user: User;
}
