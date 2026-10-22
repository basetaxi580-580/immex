import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ImmexService } from './immex.service';
import { AuditEvent, DashboardInventorySummary, ImmexDashboardSummary, ImmexRecord, LoteMaterialView } from './models';

@Component({
  selector: 'app-auditor-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <main class="auditor-page">
      <header class="card hero">
        <div>
          <p class="eyebrow">IMMEX · Rol auditor</p>
          <h1>Vista de solo lectura</h1>
          <p class="subtitle">Acceso de consulta a inventario, bitácora y estado operativo sin acciones de escritura.</p>
        </div>
        <div class="metrics">
          <div><span>Registros</span><strong>{{ summary?.totalRecords ?? records.length }}</strong></div>
          <div><span>Saldo total</span><strong>{{ inventorySummary?.saldoTotalMxn | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
          <div><span>Eventos</span><strong>{{ auditEvents.length }}</strong></div>
          <div><span>Alertas</span><strong>{{ inventorySummary?.materiales?.filter(item => item.diasRestantes <= 90).length ?? 0 }}</strong></div>
          <div><span>Conector 12p</span><strong>{{ conector12Rows[0]?.diasRestantes ?? '—' }}</strong><small>{{ conector12Rows[0]?.fechaVencimiento ? (conector12Rows[0].fechaVencimiento | date:'shortDate') : '—' }}</small></div>
        </div>
      </header>

      <section class="card block">
        <div class="card-head">
          <h2>Inventario crítico</h2>
          <span>Lectura completa</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Material</th>
                <th>Saldo</th>
                <th>Vencimiento</th>
                <th>Monto en riesgo</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let row of (inventorySummary?.materiales ?? [])">
                <td><strong>{{ row.material }}</strong></td>
                <td>{{ row.saldo }}</td>
                <td>{{ row.fechaVencimiento ? (row.fechaVencimiento | date:'shortDate') : '—' }}</td>
                <td>{{ row.montoEnRiesgo | currency:'MXN':'symbol':'1.0-0' }}</td>
                <td>{{ row.estado }} · {{ row.diasRestantes }} días</td>
              </tr>
              <tr *ngIf="!(inventorySummary?.materiales?.length)">
                <td colspan="5" class="empty">Sin materiales para mostrar.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card block">
        <div class="card-head">
          <h2>Bitácora de solo agregar</h2>
          <span>quién, cuándo, qué y por qué</span>
        </div>
        <div class="audit-list" *ngIf="auditEvents.length; else noAudit">
          <article class="audit-item" *ngFor="let event of auditEvents">
            <strong>{{ event.action }} · {{ event.entityType }}</strong>
            <span>{{ event.actor }} · {{ event.reason || 'Sin motivo' }} · {{ event.createdAt | date:'short' }}</span>
            <p>{{ event.details }}</p>
          </article>
        </div>
        <ng-template #noAudit>
          <p class="empty">Sin eventos de auditoría todavía.</p>
        </ng-template>
      </section>

      <section class="card block">
        <div class="card-head">
          <h2>Resumen operativo</h2>
          <span>solo lectura</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Pedimento</th>
                <th>Material</th>
                <th>Movimiento</th>
                <th>Cantidad</th>
                <th>Estatus</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let record of records.slice(0, 10)">
                <td>{{ record.pedimento }}</td>
                <td>{{ record.material }}</td>
                <td>{{ record.movimiento }}</td>
                <td>{{ record.cantidad }}</td>
                <td>{{ record.status }}</td>
              </tr>
              <tr *ngIf="!records.length">
                <td colspan="5" class="empty">Sin registros operativos.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  `,
  styleUrl: './auditor.page.css',
})
export class AuditorPageComponent implements OnInit {
  private readonly service = inject(ImmexService);
  private readonly cdr = inject(ChangeDetectorRef);

  records: ImmexRecord[] = [];
  summary: ImmexDashboardSummary | null = null;
  inventorySummary: DashboardInventorySummary | null = null;
  auditEvents: AuditEvent[] = [];
  conector12Rows: LoteMaterialView[] = [];

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.service.list().subscribe({ next: (data) => { this.records = data; this.cdr.markForCheck(); }, error: () => (this.records = []) });
    this.service.summary().subscribe({ next: (data) => { this.summary = data; this.cdr.markForCheck(); }, error: () => (this.summary = null) });
    this.service.inventorySummary().subscribe({ next: (data) => { this.inventorySummary = data; this.cdr.markForCheck(); }, error: () => (this.inventorySummary = null) });
    this.service.auditEvents().subscribe({ next: (data) => { this.auditEvents = data; this.cdr.markForCheck(); }, error: () => (this.auditEvents = []) });
    this.service.lotes('Conector 12 pines').subscribe({ next: (data) => { this.conector12Rows = data; this.cdr.markForCheck(); }, error: () => (this.conector12Rows = []) });
  }
}
