// src/app/core/guards/role.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, UserRole } from '../services/auth.service';

/**
 * Guard de role — protege rotas que exigem um role específico.
 *
 * Uso nas rotas:
 *   canActivate: [authGuard, roleGuard('ADMIN')]
 *   canActivate: [authGuard, roleGuard('ADMIN', 'SUPORTE')]
 */
export function roleGuard(...allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.hasRole(...allowedRoles)) {
      return true;
    }

    // Redireciona para dashboard com mensagem de acesso negado
    router.navigate(['/dashboard'], {
      queryParams: { acessoNegado: true }
    });
    return false;
  };
}

