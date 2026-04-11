import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatListModule, MatIconModule,
    MatButtonModule, MatDividerModule, MatChipsModule
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {
  readonly auth = inject(AuthService);

  userName = '';
  userRole = '';

  constructor() {
    this.auth.currentUser$.subscribe(user => {
      this.userName = user?.nome ?? '';
      this.userRole = user?.role ?? '';
    });
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  get isAdminOrSuporte(): boolean {
    return this.auth.isAdminOrSuporte();
  }

  logout() {
    this.auth.logout();
  }
}
