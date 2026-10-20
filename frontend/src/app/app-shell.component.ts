import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faBars,
  faBoxesStacked,
  faChartLine,
  faChevronLeft,
  faClockRotateLeft,
  faClipboardList,
  faFileCircleCheck,
  faFileInvoiceDollar,
  faHouse,
  faMagnifyingGlass,
  faTruckRampBox,
  faTriangleExclamation,
  faUserShield,
} from '@fortawesome/free-solid-svg-icons';

type SidebarLink = {
  label: string;
  description: string;
  route: string;
  fragment?: string;
  icon: typeof faHouse;
  exact?: boolean;
};

type SidebarGroup = {
  heading: string;
  items: SidebarLink[];
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, FontAwesomeModule],
  template: `
    <div class="app-shell">
      <aside class="sidebar" [class.is-collapsed]="!isSidebarOpen">
        <div class="sidebar-top">
          <div class="brand">
            <div class="brand-mark">I</div>
            <div class="brand-text">
              <p>IMMEX</p>
              <span>Panel operativo</span>
            </div>
          </div>

          <button
            type="button"
            class="sidebar-toggle"
            (click)="toggleSidebar()"
            [attr.aria-label]="isSidebarOpen ? 'Contraer menú' : 'Expandir menú'"
            [attr.title]="isSidebarOpen ? 'Contraer menú' : 'Expandir menú'"
          >
            <fa-icon [icon]="isSidebarOpen ? faChevronLeft : faBars"></fa-icon>
          </button>
        </div>

        <nav class="menu" aria-label="Navegación principal">
          <section class="menu-group" *ngFor="let group of navGroups">
            <p class="menu-heading">{{ group.heading }}</p>

            <a
              *ngFor="let item of group.items"
              class="menu-item"
              routerLinkActive="active"
              [routerLink]="item.route"
              [fragment]="item.fragment || undefined"
              [routerLinkActiveOptions]="{ exact: item.exact ?? false }"
              (click)="collapseOnMobile()"
              [attr.title]="item.label"
            >
              <span class="menu-icon"><fa-icon [icon]="item.icon"></fa-icon></span>
              <span class="menu-copy">
                <span class="menu-title">{{ item.label }}</span>
                <small>{{ item.description }}</small>
              </span>
            </a>
          </section>
        </nav>

        <div class="side-note">
          <p>Vista modular</p>
          <span>Secciones organizadas para navegación rápida y limpia.</span>
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
export class AppShellComponent {
  protected readonly faBars = faBars;
  protected readonly faChevronLeft = faChevronLeft;

  protected isSidebarOpen = typeof window !== 'undefined' && window.innerWidth > 1100;

  protected readonly navGroups: SidebarGroup[] = [
    {
      heading: 'Visión general',
      items: [
        {
          label: 'Panel general',
          description: 'Resumen ejecutivo y métricas clave',
          route: '/dashboard',
          fragment: 'dashboard',
          icon: faChartLine,
          exact: true,
        },
        {
          label: 'Inventario / Pedimentos',
          description: 'Altas, filtros y edición',
          route: '/inventory',
          fragment: 'inventory-hero',
          icon: faBoxesStacked,
          exact: true,
        },
      ],
    },
    {
      heading: 'Operación',
      items: [
        {
          label: 'Captura de movimiento',
          description: 'Alta y edición del registro',
          route: '/dashboard',
          fragment: 'form-card',
          icon: faClipboardList,
        },
        {
          label: 'Ledger IMMEX',
          description: 'Tabla detallada de movimientos',
          route: '/dashboard',
          fragment: 'list-card',
          icon: faFileInvoiceDollar,
        },
        {
          label: 'Flujo general',
          description: 'Entrada, consumo y exportación',
          route: '/dashboard',
          fragment: 'flow-section',
          icon: faTruckRampBox,
        },
      ],
    },
    {
      heading: 'Análisis y control',
      items: [
        {
          label: 'Resumen de operación',
          description: 'Conteos y saldos generales',
          route: '/dashboard',
          fragment: 'summary-section',
          icon: faChartLine,
        },
        {
          label: 'Excepciones y riesgo',
          description: 'Alertas y materiales críticos',
          route: '/dashboard',
          fragment: 'risk-section',
          icon: faTriangleExclamation,
        },
        {
          label: 'PEPS',
          description: 'Consumo simulado',
          route: '/dashboard',
          fragment: 'peps-section',
          icon: faClockRotateLeft,
        },
        {
          label: 'Cierre mensual',
          description: 'Reporte listo para revisión',
          route: '/dashboard',
          fragment: 'close-section',
          icon: faFileCircleCheck,
        },
        {
          label: 'Trazabilidad',
          description: 'Quién, cuándo y por qué',
          route: '/dashboard',
          fragment: 'audit-section',
          icon: faUserShield,
        },
      ],
    },
    {
      heading: 'Búsqueda',
      items: [
        {
          label: 'Buscar registros',
          description: 'Filtrar y localizar información',
          route: '/inventory',
          fragment: 'inventory-table',
          icon: faMagnifyingGlass,
          exact: true,
        },
      ],
    },
  ];

  protected toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  protected collapseOnMobile(): void {
    if (typeof window !== 'undefined' && window.innerWidth <= 980) {
      this.isSidebarOpen = false;
    }
  }
}
