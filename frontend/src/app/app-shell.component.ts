import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ImmexService } from './immex.service';
import { UserRoleService } from './user-role.service';
import { DashboardInventorySummary, DashboardMaterialRow } from './models';
import { faBars, faBoxesStacked, faChartLine, faChevronLeft, faClockRotateLeft, faClipboardList, faFileCircleCheck, faFileInvoiceDollar, faGear, faHouse, faMagnifyingGlass, faTruckRampBox, faTriangleExclamation, faUserShield, faWandMagicSparkles } from '@fortawesome/free-solid-svg-icons';

type AlertItem = DashboardMaterialRow & { accionSugerida: 'exportar' | 'retornar' | 'importar en definitiva' };

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
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterOutlet, FontAwesomeModule],
  template: `
    <div class="app-shell">
      <aside class="sidebar" [class.is-collapsed]="!isSidebarOpen">
        <div class="sidebar-top">
          <div class="brand">
            <img class="brand-logo" src="assets/aura24-logo.png" alt="AURA24 — Administración Unificada de Riesgo y Anexo 24" />
            <div class="brand-text">
              <p>Control IMMEX</p>
              <span>Panel operativo</span>
            </div>
          </div>

          <button type="button" class="sidebar-toggle" (click)="toggleSidebar()" [attr.aria-label]="isSidebarOpen ? 'Contraer menú' : 'Expandir menú'" [attr.title]="isSidebarOpen ? 'Contraer menú' : 'Expandir menú'">
            <fa-icon [icon]="isSidebarOpen ? faChevronLeft : faBars"></fa-icon>
          </button>
        </div>

        <div class="company-selector">
          <label>
            <span>Razón social / RFC</span>
            <select [(ngModel)]="selectedRazonSocial" name="selectedRazonSocial" (ngModelChange)="changeCompanyFilter($event)">
              <option value="all">Vista consolidada</option>
              <option *ngFor="let option of razonSociales" [value]="option.nombre">{{ option.nombre }} · {{ option.rfc }}</option>
            </select>
          </label>
          <small>{{ selectedRfcLabel }}</small>
        </div>

        <div class="company-selector">
          <label>
            <span>Rol actual</span>
            <select [(ngModel)]="selectedRole" name="selectedRole" (ngModelChange)="changeRole($event)">
              <option value="Especialista">Especialista</option>
              <option value="Consulta">Consulta</option>
            </select>
          </label>
          <small>{{ selectedRole === 'Consulta' ? 'Acceso de solo lectura' : 'Acceso de edición y operación' }}</small>
        </div>

        <a class="ai-nav-link" routerLink="/assistant" routerLinkActive="active" (click)="collapseOnMobile()">
          <span class="ai-nav-icon"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span>
          <span class="ai-nav-copy"><strong>Copiloto IMMEX</strong><small>IA para cumplimiento</small></span>
          <span class="ai-nav-badge">NUEVO</span>
        </a>

        <nav class="menu" aria-label="Navegación principal">
          <section class="menu-group" *ngFor="let group of navGroups">
            <button type="button" class="menu-group-toggle" [class.active]="isGroupActive(group)" (click)="toggleGroup(group.heading)" [attr.aria-expanded]="isGroupExpanded(group.heading)" [attr.title]="group.heading">
              <span class="menu-icon"><fa-icon [icon]="group.icon"></fa-icon></span>
              <span class="menu-copy menu-group-copy">
                <span class="menu-title">{{ group.heading }}</span>
              </span>
              <span class="menu-caret" aria-hidden="true">›</span>
            </button>

            <div class="menu-subitems" *ngIf="isSidebarOpen && isGroupExpanded(group.heading)">
              <a *ngFor="let item of group.items" class="menu-item" routerLinkActive="active" [routerLink]="item.route" [routerLinkActiveOptions]="{ exact: item.exact ?? false }" (click)="collapseOnMobile()" [attr.title]="item.label">
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
          <div class="topbar-actions">
            <button type="button" class="alert-bell" (click)="toggleAlerts()" [attr.aria-expanded]="alertsOpen" aria-label="Alertas de vencimiento" title="Alertas de vencimiento">
              <fa-icon [icon]="faTriangleExclamation"></fa-icon>
              <span class="alert-count" *ngIf="alertItems.length">{{ alertItems.length }}</span>
            </button>
            <div class="alerts-panel" *ngIf="alertsOpen">
              <div class="alerts-head">
                <strong>Alertas de vencimiento</strong>
                <span>{{ alertItems.length }} activas</span>
              </div>
              <div class="alerts-empty" *ngIf="!alertItems.length">Sin alertas activas.</div>
              <div class="alerts-list" *ngIf="alertItems.length">
                <article class="alert-item" *ngFor="let item of alertItems">
                  <div>
                    <strong>{{ item.material }}</strong>
                    <span>{{ item.montoEnRiesgo | currency:'MXN':'symbol':'1.0-0' }}</span>
                  </div>
                  <p>{{ item.diasRestantes }} días · Sugerencia: {{ item.accionSugerida }}</p>
                </article>
              </div>
            </div>
            <div class="topbar-meta">
              <span>Instancia: immex-vm</span>
              <span>Swagger activo</span>
            </div>
          </div>
        </header>

        <div class="page-host" style="flex: 1 1 auto; min-height: 0; overflow-y: auto; padding: 20px;">
          <router-outlet></router-outlet>
        </div>
      </section>
    </div>
  `,
  styleUrl: './app-shell.component.css',
})
export class AppShellComponent implements OnInit {
  private readonly service = inject(ImmexService);
  private readonly roleService = inject(UserRoleService);
  protected readonly faBars = faBars;
  protected readonly faChevronLeft = faChevronLeft;
  protected readonly faTriangleExclamation = faTriangleExclamation;
  protected readonly faWandMagicSparkles = faWandMagicSparkles;

  protected isSidebarOpen = typeof window !== 'undefined' && window.innerWidth > 1100;
  protected alertsOpen = false;
  protected inventorySummary: DashboardInventorySummary | null = null;
  protected selectedRazonSocial = this.service.getCompanyFilter();
  protected selectedRole = this.roleService.selectedRole;
  protected razonSociales = [
    { nombre: 'Aerosistemas del Norte, S.A. de C.V.', rfc: 'ASN9403159K2' },
    { nombre: 'Componentes Industriales del Bajío, S.A. de C.V.', rfc: 'CIB0209187Q4' },
  ];
  protected readonly expandedGroups = new Set<string>(['Dashboard', 'Importaciones', 'Inventario', 'Producción', 'Exportaciones', 'Descargos', 'Alertas', 'Conciliación', 'Reportes', 'Configuración']);

  protected readonly navGroups: SidebarGroup[] = [
    { heading: 'Dashboard', icon: faChartLine, items: [{ label: 'Dashboard', description: 'Resumen ejecutivo y métricas clave', route: '/dashboard', icon: faChartLine, exact: true }] },
    { heading: 'Importaciones', icon: faBoxesStacked, items: [
      { label: 'Inventario / Pedimentos', description: 'Altas, filtros y edición', route: '/inventory', icon: faBoxesStacked, exact: true },
      { label: 'Captura de movimiento', description: 'Alta y edición del registro', route: '/dashboard/movement', icon: faClipboardList, exact: true },
      { label: 'Ledger IMMEX', description: 'Tabla detallada de movimientos', route: '/dashboard/ledger', icon: faFileInvoiceDollar, exact: true },
    ] },
    { heading: 'Inventario', icon: faBoxesStacked, items: [
      { label: 'Flujo general', description: 'Entrada, consumo y exportación', route: '/dashboard/flow', icon: faTruckRampBox, exact: true },
      { label: 'Resumen de operación', description: 'Conteos y saldos generales', route: '/dashboard/summary', icon: faChartLine, exact: true },
      { label: 'Búsqueda rápida', description: 'Filtrar y localizar información', route: '/search', icon: faMagnifyingGlass, exact: true },
    ] },
    { heading: 'Producción', icon: faFileCircleCheck, items: [
      { label: 'Producción mensual', description: 'Simular consumo por BOM', route: '/dashboard/production', icon: faFileCircleCheck, exact: true },
    ] },
    { heading: 'Exportaciones', icon: faTruckRampBox, items: [
      { label: 'Exportación', description: 'Salida y registro documental', route: '/dashboard/flow', icon: faTruckRampBox, exact: true },
      { label: 'Cierre mensual', description: 'Reporte listo para revisión', route: '/dashboard/close', icon: faFileCircleCheck, exact: true },
    ] },
    { heading: 'Descargos', icon: faClockRotateLeft, items: [
      { label: 'PEPS', description: 'Consumo simulado y reversión', route: '/dashboard/peps', icon: faClockRotateLeft, exact: true },
    ] },
    { heading: 'Alertas', icon: faTriangleExclamation, items: [
      { label: 'Excepciones y riesgo', description: 'Alertas y materiales críticos', route: '/dashboard/risk', icon: faTriangleExclamation, exact: true },
    ] },
    { heading: 'Conciliación', icon: faUserShield, items: [
      { label: 'Trazabilidad', description: 'Quién, cuándo y por qué', route: '/dashboard/audit', icon: faUserShield, exact: true },
      { label: 'Conciliación', description: 'Revisión documental y control', route: '/dashboard/audit', icon: faUserShield, exact: true },
    ] },
    { heading: 'Reportes', icon: faFileCircleCheck, items: [
      { label: 'Anexo 24', description: 'Excel por lote', route: '/dashboard/close', icon: faFileCircleCheck, exact: true },
      { label: 'Auditoría', description: 'Historial de cambios', route: '/dashboard/audit', icon: faUserShield, exact: true },
    ] },
    { heading: 'Configuración', icon: faGear, items: [{ label: 'Configuración', description: 'Razón social y rol actual', route: '/dashboard', icon: faGear, exact: true }] },
  ];

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.loadInventorySummary();
  }

  protected get selectedRfcLabel(): string {
    if (this.selectedRazonSocial === 'all') return `${this.razonSociales.length} empresas incluidas`;
    return this.razonSociales.find((option) => option.nombre === this.selectedRazonSocial)?.rfc ?? '—';
  }

  protected get alertItems(): AlertItem[] {
    const rows = this.inventorySummary?.materiales ?? [];
    const seen = new Set<string>();
    return rows
      .filter((row: DashboardMaterialRow) => row.diasRestantes <= 90)
      .map((row: DashboardMaterialRow) => ({ ...row, accionSugerida: this.suggestAction(row.diasRestantes) }))
      .filter((row: AlertItem) => {
        const key = `${row.material}|${row.fechaVencimiento ?? ''}|${row.accionSugerida}`;
        if (seen.has(key)) return false;
        seen.add(key);
        return true;
      })
      .sort((a: AlertItem, b: AlertItem) => a.diasRestantes - b.diasRestantes);
  }

  protected toggleAlerts(): void {
    this.alertsOpen = !this.alertsOpen;
    if (this.alertsOpen && !this.inventorySummary) this.loadInventorySummary();
  }

  private loadInventorySummary(): void {
    this.service.inventorySummary().subscribe({ next: (data) => (this.inventorySummary = data), error: () => (this.inventorySummary = null) });
  }

  private suggestAction(diasRestantes: number): AlertItem['accionSugerida'] {
    if (diasRestantes <= 30) return 'importar en definitiva';
    if (diasRestantes <= 60) return 'retornar';
    return 'exportar';
  }

  protected changeCompanyFilter(value: string): void {
    this.selectedRazonSocial = value || 'all';
    this.service.setCompanyFilter(this.selectedRazonSocial);
    this.loadInventorySummary();
    this.alertsOpen = false;
  }

  protected changeRole(value: string): void {
    this.selectedRole = value === 'Consulta' ? 'Consulta' : 'Especialista';
    this.roleService.setSelectedRole(this.selectedRole);
  }

  protected toggleSidebar(): void { this.isSidebarOpen = !this.isSidebarOpen; }

  protected toggleGroup(groupHeading: string): void {
    if (!this.isSidebarOpen) {
      this.isSidebarOpen = true;
      this.expandedGroups.add(groupHeading);
      return;
    }
    if (this.expandedGroups.has(groupHeading)) this.expandedGroups.delete(groupHeading); else this.expandedGroups.add(groupHeading);
  }

  protected isGroupExpanded(groupHeading: string): boolean { return this.expandedGroups.has(groupHeading); }
  protected isGroupActive(group: SidebarGroup): boolean { return group.items.some((item) => this.router.url === item.route); }
  protected collapseOnMobile(): void { if (typeof window !== 'undefined' && window.innerWidth <= 980) this.isSidebarOpen = false; }
}
