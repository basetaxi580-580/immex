import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
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
  icon: typeof faHouse;
  exact?: boolean;
};

type SidebarGroup = {
  heading: string;
  icon: typeof faHouse;
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
            <button
              type="button"
              class="menu-group-toggle"
              [class.active]="isGroupActive(group)"
              (click)="toggleGroup(group.heading)"
              [attr.aria-expanded]="isGroupExpanded(group.heading)"
              [attr.title]="group.heading"
            >
              <span class="menu-icon"><fa-icon [icon]="group.icon"></fa-icon></span>
              <span class="menu-copy menu-group-copy">
                <span class="menu-title">{{ group.heading }}</span>
              </span>
              <span class="menu-caret" aria-hidden="true">›</span>
            </button>

            <div class="menu-subitems" *ngIf="isSidebarOpen && isGroupExpanded(group.heading)">
              <a
                *ngFor="let item of group.items"
                class="menu-item"
                routerLinkActive="active"
                [routerLink]="item.route"
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
            </div>
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
  protected readonly expandedGroups = new Set<string>([
    'Panel general',
    'Inventario y movimientos',
    'Control y seguimiento',
    'Reportes y auditoría',
    'Búsqueda',
  ]);

  protected readonly navGroups: SidebarGroup[] = [
    {
      heading: 'Panel general',
      icon: faChartLine,
      items: [
        {
          label: 'Panel general',
          description: 'Resumen ejecutivo y métricas clave',
          route: '/dashboard',
          icon: faChartLine,
          exact: true,
        },
      ],
    },
    {
      heading: 'Inventario y movimientos',
      icon: faBoxesStacked,
      items: [
        {
          label: 'Inventario / Pedimentos',
          description: 'Altas, filtros y edición',
          route: '/inventory',
          icon: faBoxesStacked,
          exact: true,
        },
        {
          label: 'Captura de movimiento',
          description: 'Alta y edición del registro',
          route: '/dashboard/movement',
          icon: faClipboardList,
          exact: true,
        },
        {
          label: 'Ledger IMMEX',
          description: 'Tabla detallada de movimientos',
          route: '/dashboard/ledger',
          icon: faFileInvoiceDollar,
          exact: true,
        },
        {
          label: 'Flujo general',
          description: 'Entrada, consumo y exportación',
          route: '/dashboard/flow',
          icon: faTruckRampBox,
          exact: true,
        },
      ],
    },
    {
      heading: 'Control y seguimiento',
      icon: faChartLine,
      items: [
        {
          label: 'Resumen de operación',
          description: 'Conteos y saldos generales',
          route: '/dashboard/summary',
          icon: faChartLine,
          exact: true,
        },
        {
          label: 'Excepciones y riesgo',
          description: 'Alertas y materiales críticos',
          route: '/dashboard/risk',
          icon: faTriangleExclamation,
          exact: true,
        },
        {
          label: 'PEPS',
          description: 'Consumo simulado',
          route: '/dashboard/peps',
          icon: faClockRotateLeft,
          exact: true,
        },
      ],
    },
    {
      heading: 'Reportes y auditoría',
      icon: faFileCircleCheck,
      items: [
        {
          label: 'Producción mensual',
          description: 'Simular consumo por BOM',
          route: '/dashboard/production',
          icon: faFileCircleCheck,
          exact: true,
        },
        {
          label: 'Cierre mensual',
          description: 'Reporte listo para revisión',
          route: '/dashboard/close',
          icon: faFileCircleCheck,
          exact: true,
        },
        {
          label: 'Trazabilidad',
          description: 'Quién, cuándo y por qué',
          route: '/dashboard/audit',
          icon: faUserShield,
          exact: true,
        },
      ],
    },
    {
      heading: 'Búsqueda',
      icon: faMagnifyingGlass,
      items: [
        {
          label: 'Búsqueda rápida',
          description: 'Filtrar y localizar información',
          route: '/search',
          icon: faMagnifyingGlass,
          exact: true,
        },
      ],
    },
  ];

  constructor(private readonly router: Router) {}

  protected toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  protected toggleGroup(groupHeading: string): void {
    if (!this.isSidebarOpen) {
      this.isSidebarOpen = true;
      this.expandedGroups.add(groupHeading);
      return;
    }

    if (this.expandedGroups.has(groupHeading)) {
      this.expandedGroups.delete(groupHeading);
      return;
    }

    this.expandedGroups.add(groupHeading);
  }

  protected isGroupExpanded(groupHeading: string): boolean {
    return this.expandedGroups.has(groupHeading);
  }

  protected isGroupActive(group: SidebarGroup): boolean {
    return group.items.some((item) => this.router.url === item.route);
  }

  protected collapseOnMobile(): void {
    if (typeof window !== 'undefined' && window.innerWidth <= 980) {
      this.isSidebarOpen = false;
    }
  }
}
