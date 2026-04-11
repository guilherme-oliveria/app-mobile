// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./modules/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./modules/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'pedidos',
        loadComponent: () => import('./modules/pedidos/pedidos.component').then(m => m.PedidosComponent)
      },
      {
        path: 'motoboys',
        loadComponent: () => import('./modules/motoboys/motoboys.component').then(m => m.MotoboysComponent)
      },
      {
        path: 'lojas',
        loadComponent: () => import('./modules/lojas/lojas.component').then(m => m.LojasComponent)
      },
      {
        // ❌ SUPORTE não acessa relatórios — apenas ADMIN
        path: 'relatorios',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./modules/relatorios/relatorios.component').then(m => m.RelatoriosComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
