// src/app/core/services/auth.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginResponse {
  token: string;
  role: string;
  nome: string;
  refId?: number | null;
}

export type UserRole = 'ADMIN' | 'SUPORTE' | 'LOJA' | 'MOTOBOY';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private tokenKey = 'delivery_token';
  private userKey = 'delivery_user';

  currentUser$ = new BehaviorSubject<LoginResponse | null>(this.getStoredUser());

  /** Role do usuário logado como observable */
  role$ = this.currentUser$.pipe(map(u => u?.role as UserRole | null));

  /** Verifica se o usuário logado tem um dos roles informados */
  hasRole(...roles: UserRole[]): boolean {
    const role = this.currentUser$.value?.role as UserRole;
    return roles.includes(role);
  }

  /** True somente para ADMIN */
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /** True para ADMIN ou SUPORTE */
  isAdminOrSuporte(): boolean {
    return this.hasRole('ADMIN', 'SUPORTE');
  }

  login(email: string, senha: string) {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(tap(res => {
        localStorage.setItem(this.tokenKey, res.token);
        localStorage.setItem(this.userKey, JSON.stringify(res));
        this.currentUser$.next(res);
      }));
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUser$.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private getStoredUser(): LoginResponse | null {
    const stored = localStorage.getItem(this.userKey);
    return stored ? JSON.parse(stored) : null;
  }
}
