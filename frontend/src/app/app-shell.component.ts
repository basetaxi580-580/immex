import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">I</div>
          <div>
            <p>IMMEX</p>
            <span>Panel operativo</span>
          </div>
        </div>

        <nav class="menu">
          <a routerLink="/dashboard" routerLinkActive="active">
            <span>Dashboard</span>
            <small>Resumen y control</small>
          </a>
          <a routerLink="/inventory" routerLinkActive="active">
            <span>Inventario / Pedimentos</span>
            <small>Altas, filtros y edición</small>
          </a>
        </nav>

        <div class="side-note">
          <p>Vista modular</p>
          <span>El resto de módulos se agregan uno por uno.</span>
        </div>
      </aside>

      <section class="workspace">
        <header class="topbar">
          <div>
            <p class="eyebrow">IMMEX / GCP</p>
            <h1>Panel de operación</h1>
          </div>
          <div class="topbar-meta">
            <span>Instancia: immex-vm</span>
            <span>Swagger activo</span>
          </div>
        </header>

        <div class="page-host">
          <router-outlet></router-outlet>
        </div>
      </section>
    </div>
  `,
  styleUrl: './app-shell.component.css',
})
export class AppShellComponent {}
