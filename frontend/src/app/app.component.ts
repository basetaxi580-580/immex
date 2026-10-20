import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ImmexService } from './immex.service';
import { ImmexCloseReport, ImmexDashboardSummary, ImmexRecord, PepsOperationResult } from './models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="shell">
      <section class="hero document-header">
        <div class="hero-copy">
          <p class="eyebrow">IMMEX</p>
          <h1>Programa IMMEX</h1>
          <hr class="divider" />
          <p class="subtitle">
            Aquí encontrarás diversa información que deberás considerar para poder llevar a cabo tus operaciones bajo
            el Programa de la Industria Manufacturera, Maquiladora y de Servicios de Exportación (IMMEX).
          </p>
          <p class="note-link">
            <a href="https://www.snice.gob.mx/~oracle/SNICE_DOCS/RAOCE-2026-PROGRAMAS-DE-FOMENTO_20260423-20260423.pdf" target="_blank" rel="noopener">
              Consulta aquí la guía de VUCEM para el reporte anual de operaciones de Comercio Exterior.
            </a>
          </p>
          <p class="note-link">
            <a href="https://www.snice.gob.mx/cs/avi/snice/aviso.disponibilidad.html" target="_blank" rel="noopener">
              Aquí podrás consultar el Aviso de disponibilidad de notificación.
            </a>
          </p>
          <p class="contact-line">
            ¿Tienes alguna duda? Envíanos un correo a: <strong>atencion.immex@economia.gob.mx</strong>
          </p>
        </div>
      </section>

      <section class="grid">
        <article class="card form-card">
          <div class="card-head">
            <h2>{{ editingId ? 'Editar movimiento' : 'Nuevo movimiento' }}</h2>
            <span>{{ editingId ? 'Actualización' : 'Alta rápida' }}</span>
          </div>

          <form class="form" (ngSubmit)="save()">
            <label>
              <span>Pedimento / Folio</span>
              <input [(ngModel)]="form.pedimento" name="pedimento" required placeholder="PI-2401-001" />
            </label>
            <label>
              <span>Material</span>
              <input [(ngModel)]="form.material" name="material" required placeholder="Resina ABS" />
            </label>
            <label>
              <span>Proveedor / Planta</span>
              <input [(ngModel)]="form.proveedor" name="proveedor" required placeholder="Planta norte" />
            </label>
            <label>
              <span>Auditor / responsable</span>
              <input [(ngModel)]="form.auditor" name="auditor" placeholder="Especialista aduanal" />
            </label>
            <label>
              <span>Movimiento</span>
              <select [(ngModel)]="form.movimiento" name="movimiento" required>
                <option value="Entrada">Entrada</option>
                <option value="Consumo">Consumo</option>
                <option value="Exportación">Exportación</option>
                <option value="Ajuste">Ajuste</option>
              </select>
            </label>
            <div class="two-cols">
              <label>
                <span>Cantidad</span>
                <input [(ngModel)]="form.cantidad" name="cantidad" type="number" min="1" required />
              </label>
              <label>
                <span>Valor unitario</span>
                <input [(ngModel)]="form.valorUnitario" name="valorUnitario" type="number" step="0.01" min="0" required />
              </label>
            </div>
            <div class="two-cols">
              <label>
                <span>Fecha ingreso</span>
                <input [(ngModel)]="form.fechaIngreso" name="fechaIngreso" type="date" required />
              </label>
              <label>
                <span>Fecha vencimiento</span>
                <input [(ngModel)]="form.fechaVencimiento" name="fechaVencimiento" type="date" required />
              </label>
            </div>
            <label>
              <span>Estatus</span>
              <select [(ngModel)]="form.status" name="status" required>
                <option value="Vigente">Vigente</option>
                <option value="Por vencer">Por vencer</option>
                <option value="Cerrado">Cerrado</option>
                <option value="Conciliación">Conciliación</option>
              </select>
            </label>
            <label class="check-row">
              <input [(ngModel)]="form.activo" name="activo" type="checkbox" />
              <span>Activo</span>
            </label>
            <label>
              <span>Notas</span>
              <textarea [(ngModel)]="form.notas" name="notas" rows="3" placeholder="Observaciones de conciliación"></textarea>
            </label>

            <div class="actions">
              <button class="primary" type="submit">{{ editingId ? 'Actualizar' : 'Guardar' }}</button>
              <button class="ghost" type="button" (click)="resetForm()">Limpiar</button>
            </div>
          </form>

          <p class="error" *ngIf="error">{{ error }}</p>
        </article>

        <article class="card list-card">
          <div class="card-head">
            <h2>Ledger IMMEX</h2>
            <span>{{ records.length }} movimientos</span>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Pedimento</th>
                  <th>Material</th>
                  <th>Movimiento</th>
                  <th>Cantidad</th>
                  <th>Vencimiento</th>
                  <th>Días</th>
                  <th>Riesgo</th>
                  <th>Estatus</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let record of records">
                  <td>{{ record.pedimento }}</td>
                  <td>
                    <strong>{{ record.material }}</strong>
                    <div class="muted">{{ record.proveedor }}</div>
                    <div class="muted" *ngIf="record.auditor">{{ record.auditor }}</div>
                  </td>
                  <td>{{ record.movimiento }}</td>
                  <td>{{ record.cantidad }}</td>
                  <td>{{ formatDate(record.fechaVencimiento) }}</td>
                  <td>
                    <span class="badge" [class.warning]="daysLeft(record) <= 60" [class.danger]="daysLeft(record) <= 30">
                      {{ daysLeft(record) }} días
                    </span>
                  </td>
                  <td>{{ riskFor(record) | currency:'MXN':'symbol':'1.0-0' }}</td>
                  <td><span class="badge">{{ record.status }}</span></td>
                  <td>
                    <div class="row-actions">
                      <button type="button" class="link" (click)="edit(record)">Editar</button>
                      <button type="button" class="link danger" (click)="remove(record)">Eliminar</button>
                    </div>
                  </td>
                </tr>
                <tr *ngIf="!records.length">
                  <td colspan="9" class="empty">Aún no hay movimientos.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section class="module-grid">
        <article class="card module-card">
          <div class="card-head">
            <h2>Flujo general</h2>
            <span>Entrada → consumo → exportación</span>
          </div>
          <ol class="flow-list">
            <li><strong>Entrada:</strong> pedimento importado entra al ledger.</li>
            <li><strong>Consumo PEPS:</strong> sale primero el material más antiguo.</li>
            <li><strong>Exportación:</strong> descarga el saldo comprometido.</li>
            <li><strong>Cierre:</strong> se consolida el reporte Anexo 24.</li>
          </ol>
        </article>

        <article class="card module-card">
          <div class="card-head">
            <h2>Resumen de operación</h2>
            <span>Último corte</span>
          </div>
          <div class="summary-grid">
            <div><span>Entradas</span><strong>{{ entryCount }}</strong></div>
            <div><span>Consumos</span><strong>{{ consumptionCount }}</strong></div>
            <div><span>Exportaciones</span><strong>{{ exportCount }}</strong></div>
            <div><span>Listos para cierre</span><strong>{{ closeReadyCount }}</strong></div>
          </div>
        </article>

        <article class="card module-card wide-card">
          <div class="card-head">
            <h2>Excepciones y riesgo</h2>
            <span>{{ expiringSoonCount }} urgentes</span>
          </div>
          <div class="exception-list">
            <div class="exception-item" *ngFor="let record of riskAlerts">
              <strong>{{ record.material }}</strong>
              <span>{{ record.pedimento }} · {{ daysLeft(record) }} días · {{ riskFor(record) | currency:'MXN':'symbol':'1.0-0' }}</span>
            </div>
            <div class="empty" *ngIf="!riskAlerts.length">Sin excepciones por ahora.</div>
          </div>
        </article>
      </section>

      <section class="module-grid lower-grid">
        <article class="card module-card">
          <div class="card-head">
            <h2>PEPS</h2>
            <span>Consumo simulado</span>
          </div>
          <form class="form compact-form" (ngSubmit)="runPeps()">
            <label>
              <span>Material</span>
              <input [(ngModel)]="pepsForm.material" name="pepsMaterial" placeholder="Resina ABS" required />
            </label>
            <div class="two-cols">
              <label>
                <span>Cantidad</span>
                <input [(ngModel)]="pepsForm.quantity" name="pepsQuantity" type="number" min="1" required />
              </label>
              <label>
                <span>Responsable</span>
                <input [(ngModel)]="pepsForm.auditor" name="pepsAuditor" placeholder="Especialista" />
              </label>
            </div>
            <label>
              <span>Motivo</span>
              <textarea [(ngModel)]="pepsForm.reason" name="pepsReason" rows="2" placeholder="Salida para producción"></textarea>
            </label>
            <button class="primary" type="submit">Aplicar PEPS</button>
          </form>
          <p class="success" *ngIf="pepsResult">{{ pepsResult.message }}</p>
        </article>

        <article class="card module-card">
          <div class="card-head">
            <h2>Cierre mensual</h2>
            <span>Reporte listo</span>
          </div>
          <div class="close-report-box" *ngIf="closeReport; else closeReportEmpty">
            <p><strong>Total:</strong> {{ closeReport.totalRecords }}</p>
            <p><strong>Abiertos:</strong> {{ closeReport.openRecords }}</p>
            <p><strong>Por vencer:</strong> {{ closeReport.expiringSoonCount }}</p>
            <p><strong>Riesgo:</strong> {{ closeReport.riskTotal | currency:'MXN':'symbol':'1.0-0' }}</p>
            <p *ngIf="closeReport.pepsCandidate"><strong>PEPS:</strong> {{ closeReport.pepsCandidate.material }}</p>
            <div class="warning-list" *ngIf="closeReport.warnings.length">
              <span class="warning-chip" *ngFor="let warning of closeReport.warnings">{{ warning }}</span>
            </div>
          </div>
          <ng-template #closeReportEmpty>
            <p class="muted">Sin reporte aún.</p>
          </ng-template>
          <button class="ghost" type="button" (click)="refreshCloseReport()">Actualizar cierre</button>
        </article>
      </section>

      <section class="card audit-card">
        <div class="card-head">
          <h2>Trazabilidad</h2>
          <span>quién, cuándo y por qué</span>
        </div>
        <div class="audit-list" *ngIf="auditEvents.length; else auditEmpty">
          <div class="audit-item" *ngFor="let event of auditEvents">
            <strong>{{ event.action }} · {{ event.entityType }}</strong>
            <span>{{ event.actor }} · {{ event.reason || 'Sin motivo' }} · {{ event.createdAt | date:'short' }}</span>
            <p>{{ event.details }}</p>
          </div>
        </div>
        <ng-template #auditEmpty>
          <p class="muted">Sin eventos de auditoría todavía.</p>
        </ng-template>
        <button class="ghost" type="button" (click)="refreshAudit()">Actualizar trazabilidad</button>
      </section>
    </main>
  `,
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  private readonly service = inject(ImmexService);

  records: ImmexRecord[] = [];
  summary: ImmexDashboardSummary | null = null;
  closeReport: ImmexCloseReport | null = null;
  pepsResult: PepsOperationResult | null = null;
  auditEvents: any[] = [];
  editingId: string | null = null;
  connected = true;
  error = '';
  form: ImmexRecord = this.emptyForm();
  pepsForm = {
    material: 'Resina ABS',
    quantity: 100,
    reason: 'Consumo para producción',
    auditor: 'Especialista',
  };

  ngOnInit(): void {
    this.load();
    this.refreshCloseReport();
    this.refreshAudit();
  }

  get riskTotal(): number {
    return this.summary?.riskTotal ?? this.records.reduce((sum, record) => sum + this.riskFor(record), 0);
  }

  get expiringSoonCount(): number {
    return this.summary?.expiringSoonCount ?? this.records.filter((record) => this.daysLeft(record) <= 30).length;
  }

  get alert90Count(): number {
    return this.summary?.alert90Count ?? this.records.filter((record) => {
      const days = this.daysLeft(record);
      return days <= 90 && days > 60;
    }).length;
  }

  get alert60Count(): number {
    return this.summary?.alert60Count ?? this.records.filter((record) => {
      const days = this.daysLeft(record);
      return days <= 60 && days > 30;
    }).length;
  }

  get alert30Count(): number {
    return this.summary?.alert30Count ?? this.records.filter((record) => this.daysLeft(record) <= 30).length;
  }

  get entryCount(): number {
    return this.summary?.entryCount ?? this.records.filter((record) => record.movimiento === 'Entrada').length;
  }

  get consumptionCount(): number {
    return this.summary?.consumptionCount ?? this.records.filter((record) => record.movimiento === 'Consumo').length;
  }

  get exportCount(): number {
    return this.summary?.exportCount ?? this.records.filter((record) => record.movimiento === 'Exportación').length;
  }

  get closeReadyCount(): number {
    return this.summary?.closeReadyCount ?? this.records.filter((record) => record.status === 'Cerrado' || this.daysLeft(record) > 0).length;
  }

  get riskAlerts(): ImmexRecord[] {
    const source = this.summary?.pepsCandidate ? [this.summary.pepsCandidate, ...this.records] : this.records;
    return [...source]
      .filter((record, index, self) => self.findIndex((candidate) => candidate.id === record.id) === index)
      .filter((record) => this.daysLeft(record) <= 60)
      .sort((a, b) => this.daysLeft(a) - this.daysLeft(b));
  }

  load(): void {
    this.service.list().subscribe({
      next: (data) => {
        this.records = data;
        this.connected = true;
        this.error = '';
        this.loadSummary();
      },
      error: () => {
        this.connected = false;
        this.error = 'No se pudo conectar al backend.';
      },
    });
  }

  loadSummary(): void {
    this.service.summary().subscribe({
      next: (data) => (this.summary = data),
      error: () => (this.summary = null),
    });
  }

  refreshCloseReport(): void {
    this.service.closeReport().subscribe({
      next: (data) => (this.closeReport = data),
      error: () => (this.closeReport = null),
    });
  }

  refreshAudit(): void {
    this.service.auditEvents().subscribe({
      next: (data) => (this.auditEvents = data),
      error: () => (this.auditEvents = []),
    });
  }

  runPeps(): void {
    this.service.consumePeps(this.pepsForm).subscribe({
      next: (data) => {
        this.pepsResult = data;
        this.load();
        this.refreshCloseReport();
      },
      error: () => {
        this.error = 'No se pudo aplicar PEPS.';
      },
    });
  }

  save(): void {
    const payload = this.normalizeForm();
    const request$ = this.editingId
      ? this.service.update(this.editingId, payload)
      : this.service.create(payload);

    request$.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: () => {
        this.error = 'No se pudo guardar el registro.';
      },
    });
  }

  edit(record: ImmexRecord): void {
    this.editingId = record.id ?? null;
    this.form = {
      pedimento: record.pedimento,
      material: record.material,
      proveedor: record.proveedor,
      movimiento: record.movimiento,
      cantidad: record.cantidad,
      valorUnitario: record.valorUnitario,
      fechaIngreso: record.fechaIngreso,
      fechaVencimiento: record.fechaVencimiento,
      status: record.status,
      activo: record.activo,
      notas: record.notas ?? '',
      auditor: record.auditor ?? '',
    };
  }

  remove(record: ImmexRecord): void {
    if (!record.id) return;
    this.service.remove(record.id).subscribe({
      next: () => this.load(),
      error: () => {
        this.error = 'No se pudo eliminar el registro.';
      },
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.error = '';
  }

  riskFor(record: ImmexRecord): number {
    return Number(record.cantidad || 0) * Number(record.valorUnitario || 0);
  }

  daysLeft(record: ImmexRecord): number {
    const due = new Date(record.fechaVencimiento + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return Math.ceil((due.getTime() - today.getTime()) / 86400000);
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('es-MX').format(new Date(value + 'T00:00:00'));
  }

  private emptyForm(): ImmexRecord {
    const today = new Date();
    const isoToday = today.toISOString().slice(0, 10);
    const due = new Date(today);
    due.setDate(due.getDate() + 90);
    return {
      pedimento: '',
      material: '',
      proveedor: '',
      movimiento: 'Entrada',
      cantidad: 1,
      valorUnitario: 0,
      fechaIngreso: isoToday,
      fechaVencimiento: due.toISOString().slice(0, 10),
      status: 'Vigente',
      activo: true,
      notas: '',
      auditor: 'Sistema',
    };
  }

  private normalizeForm(): ImmexRecord {
    return {
      pedimento: this.form.pedimento.trim(),
      material: this.form.material.trim(),
      proveedor: this.form.proveedor.trim(),
      movimiento: this.form.movimiento,
      cantidad: Number(this.form.cantidad),
      valorUnitario: Number(this.form.valorUnitario),
      fechaIngreso: this.form.fechaIngreso,
      fechaVencimiento: this.form.fechaVencimiento,
      status: this.form.status,
      activo: this.form.activo,
      notas: (this.form.notas ?? '').trim(),
      auditor: (this.form.auditor ?? '').trim(),
    };
  }
}
