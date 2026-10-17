import { Injectable, computed, signal } from '@angular/core';
import { Observable, delay, of, tap, throwError } from 'rxjs';
import { AuthResponse, User } from '../models/user';

const SESSION_KEY = 'bt-session';

// ⚠️ TEMPORARY: a fake "server" so we can build the frontend first.
// Replaced by real HTTP calls to Spring Boot later. Never store
// passwords in the browser in a real app.
const MOCK_USERS_KEY = 'bt-mock-users';
interface MockUser extends User {
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private session = signal<AuthResponse | null>(this.loadSession());

  user = computed(() => this.session()?.user ?? null);
  token = computed(() => this.session()?.token ?? null);
  isLoggedIn = computed(() => this.session() !== null);

  login(email: string, password: string): Observable<AuthResponse> {
    const found = this.mockUsers().find(
      (u) => u.email === email.toLowerCase() && u.password === password,
    );

    if (!found) {
      return throwError(() => new Error('Wrong email or password.'));
    }

    return of(this.toResponse(found)).pipe(
      delay(400), // pretend the network takes a moment
      tap((response) => this.saveSession(response)),
    );
  }

  signup(name: string, email: string, password: string): Observable<AuthResponse> {
    const users = this.mockUsers();
    const normalizedEmail = email.toLowerCase();

    if (users.some((u) => u.email === normalizedEmail)) {
      return throwError(() => new Error('An account with this email already exists.'));
    }

    const newUser: MockUser = { id: Date.now(), name, email: normalizedEmail, password };
    localStorage.setItem(MOCK_USERS_KEY, JSON.stringify([...users, newUser]));

    return of(this.toResponse(newUser)).pipe(
      delay(400),
      tap((response) => this.saveSession(response)),
    );
  }

  logout() {
    this.session.set(null);
    localStorage.removeItem(SESSION_KEY);
  }

  // ---------- helpers ----------
  private saveSession(response: AuthResponse) {
    this.session.set(response);
    localStorage.setItem(SESSION_KEY, JSON.stringify(response));
  }

  private loadSession(): AuthResponse | null {
    try {
      const saved = localStorage.getItem(SESSION_KEY);
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  }

  private mockUsers(): MockUser[] {
    try {
      return JSON.parse(localStorage.getItem(MOCK_USERS_KEY) ?? '[]');
    } catch {
      return [];
    }
  }

  private toResponse(mockUser: MockUser): AuthResponse {
    const { password, ...user } = mockUser;
    return { token: `mock-token-${user.id}`, user };
  }
}
