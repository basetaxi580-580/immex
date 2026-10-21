import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ImmexService } from './immex.service';
import { CollectionSnapshot, ImmexRecord } from './models';

@Component({
  selector: 'app-inventory-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="module-shell">
      <section class="module-hero card" id="inventory-hero" *ngIf="!searchOnly">
        <div>
          <p class="eyebrow">Módulo · Inventario / Pedimentos</p>
          <h1>Registro de materiales y control documental</h1>
          <p class="subtitle">
            Administra altas, cambios y bajas con búsqueda rápida, filtros por estatus y una vista clara del riesgo.
          </p>
        </div>
        <div class="module-stats">
          <div><span>Total</span><strong>{{ records.length }}</strong></div>
          <div><span>Activos</span><strong>{{ activeCount }}</strong></div>
          <div><span>Vencen ≤30 días</span><strong>{{ expiringSoonCount }}</strong></div>
          <div><span>Valor total</span><strong>{{ totalValue | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
        </div>
      </section>

      <section class="card import-card" *ngIf="!searchOnly">
        <div class="card-head">
          <h2>Captura de pedimento de importación</h2>
          <span>Alta con partidas múltiples</span>
        </div>

        <form class="form" (ngSubmit)="submitImport()" #importForm="ngForm">
          <div class="two-cols">
            <label>
              <span>Número de pedimento</span>
              <input [(ngModel)]="importFormModel.numeroPedimento" name="numeroPedimento" required placeholder="24 48 3021 9001234" />
            </label>
            <label>
              <span>Fecha</span>
              <input [(ngModel)]="importFormModel.fecha" name="fecha" type="date" required />
            </label>
          </div>

          <label>
            <span>Razón social</span>
            <select [(ngModel)]="importFormModel.razonSocial" name="razonSocial" required>
              <option value="">Selecciona una razón social</option>
              <option *ngFor="let rs of razonSociales" [value]="rs">{{ rs }}</option>
            </select>
          </label>

          <div class="partidas-head">
            <h3>Partidas</h3>
            <button type="button" class="ghost" (click)="addPartida()">Agregar partida</button>
          </div>

          <div class="partidas-list">
            <article class="partida-card" *ngFor="let partida of importFormModel.partidas; let i = index">
              <div class="partida-top">
                <strong>Partida {{ i + 1 }}</strong>
                <button type="button" class="link danger" (click)="removePartida(i)" [disabled]="importFormModel.partidas.length === 1">Quitar</button>
              </div>
              <div class="two-cols">
                <label>
                  <span>Material</span>
                  <input [(ngModel)]="partida.material" [name]="'material' + i" required placeholder="Resina ABS natural" />
                </label>
                <label>
                  <span>Cantidad</span>
                  <input [(ngModel)]="partida.cantidad" [name]="'cantidad' + i" type="number" min="1" required />
                </label>
              </div>
              <label>
                <span>Valor unitario en MXN</span>
                <input [(ngModel)]="partida.valorUnitarioMxn" [name]="'valorUnitarioMxn' + i" type="number" step="0.01" min="0.01" required />
              </label>
            </article>
          </div>

          <div class="actions">
            <button class="primary" type="submit">Guardar importación</button>
            <button class="ghost" type="button" (click)="resetImport()">Limpiar</button>
          </div>
        </form>

        <p class="error" *ngIf="importError">{{ importError }}</p>
        <p class="success" *ngIf="importResult">
          Pedimento {{ importResult.numeroPedimento }} guardado. {{ importResult.partidasCreadas }} partidas y total {{ importResult.totalMxn | currency:'MXN':'symbol':'1.0-0' }}.
        </p>
      </section>

      <section class="module-grid" id="inventory-grid">
        <article class="card filter-card">
          <div class="card-head">
            <h2>{{ searchOnly ? 'Búsqueda rápida' : 'Filtros' }}</h2>
            <span>Buscar y segmentar</span>
          </div>

          <div class="filters">
            <label>
              <span>Buscar</span>
              <input [(ngModel)]="searchTerm" name="searchTerm" placeholder="Pedimento, material o proveedor" (ngModelChange)="applyFilters()" />
            </label>

            <label>
              <span>Estatus</span>
              <select [(ngModel)]="statusFilter" name="statusFilter" (ngModelChange)="applyFilters()">
                <option value="Todos">Todos</option>
                <option value="Vigente">Vigente</option>
                <option value="Por vencer">Por vencer</option>
                <option value="Cerrado">Cerrado</option>
                <option value="Conciliación">Conciliación</option>
              </select>
            </label>

            <label>
              <span>Movimiento</span>
              <select [(ngModel)]="movementFilter" name="movementFilter" (ngModelChange)="applyFilters()">
                <option value="Todos">Todos</option>
                <option value="Entrada">Entrada</option>
                <option value="Consumo">Consumo</option>
                <option value="Exportación">Exportación</option>
                <option value="Ajuste">Ajuste</option>
              </select>
            </label>

            <button class="ghost" type="button" (click)="clearFilters()">Limpiar filtros</button>
          </div>

          <div class="compact-list">
            <div class="compact-item" *ngFor="let record of filteredRecords">
              <strong>{{ record.pedimento }}</strong>
              <span>{{ record.material }} · {{ record.proveedor }}</span>
              <small>{{ record.status }} · {{ daysLeft(record) }} días</small>
            </div>
            <p class="empty" *ngIf="!filteredRecords.length">No hay coincidencias.</p>
          </div>
        </article>

        <article class="card form-card" *ngIf="!searchOnly">
          <div class="card-head">
            <h2>{{ editingId ? 'Editar pedimento' : 'Nuevo pedimento' }}</h2>
            <span>{{ editingId ? 'Edición activa' : 'Captura rápida' }}</span>
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
            <div class="two-cols">
              <label>
                <span>Estatus</span>
                <select [(ngModel)]="form.status" name="status" required>
                  <option value="Vigente">Vigente</option>
                  <option value="Por vencer">Por vencer</option>
                  <option value="Cerrado">Cerrado</option>
                  <option value="Conciliación">Conciliación</option>
                </select>
              </label>
              <label>
                <span>Auditor</span>
                <input [(ngModel)]="form.auditor" name="auditor" placeholder="Especialista aduanal" />
              </label>
            </div>
            <label class="check-row">
              <input [(ngModel)]="form.activo" name="activo" type="checkbox" />
              <span>Activo</span>
            </label>
            <label>
              <span>Notas</span>
              <textarea [(ngModel)]="form.notas" name="notas" rows="3" placeholder="Observaciones del pedimento"></textarea>
            </label>
            <div class="actions">
              <button class="primary" type="submit">{{ editingId ? 'Actualizar' : 'Guardar' }}</button>
              <button class="ghost" type="button" (click)="resetForm()">Limpiar</button>
            </div>
          </form>

          <p class="error" *ngIf="error">{{ error }}</p>
        </article>
      </section>

      <section class="card table-card" id="inventory-table">
        <div class="card-head">
          <h2>Inventario detallado</h2>
          <span>{{ filteredRecords.length }} resultados</span>
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
                <th>Estatus</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let record of filteredRecords">
                <td>
                  <strong>{{ record.pedimento }}</strong>
                  <div class="muted">{{ record.proveedor }}</div>
                </td>
                <td>
                  <strong>{{ record.material }}</strong>
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
                <td><span class="badge">{{ record.status }}</span></td>
                <td>
                  <div class="row-actions">
                    <button type="button" class="link" (click)="edit(record)">Editar</button>
                    <button type="button" class="link danger" (click)="remove(record)">Eliminar</button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="!filteredRecords.length">
                <td colspan="8" class="empty">No hay registros para mostrar.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card collection-card" *ngIf="!searchOnly">
        <div class="card-head">
          <h2>Colecciones creadas</h2>
          <span>Datos ficticios reales</span>
        </div>
        <div class="collection-grid">
          <article class="collection-item" *ngFor="let item of collections">
            <strong>{{ item.name }}</strong>
            <span>{{ item.count }} documentos</span>
            <pre *ngIf="item.sample.length">{{ item.sample[0] }}</pre>
          </article>
        </div>
      </section>
    </main>
  `,
  styleUrl: './inventory.page.css',
})
export class InventoryPageComponent implements OnInit {
  private readonly service = inject(ImmexService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  readonly searchOnly = inject(ActivatedRoute).snapshot.data['searchOnly'] === true;

  records: ImmexRecord[] = [];
  filteredRecords: ImmexRecord[] = [];
  collections: CollectionSnapshot[] = [];
  editingId: string | null = null;
  error = '';
  importError = '';
  importResult: any = null;
  searchTerm = '';
  statusFilter = 'Todos';
  movementFilter = 'Todos';
  razonSociales = [
    'Aerosistemas del Norte, S.A. de C.V.',
    'Componentes Industriales del Bajío, S.A. de C.V.',
  ];
  importFormModel = this.emptyImportForm();
  form: ImmexRecord = this.emptyForm();

  ngOnInit(): void {
    const record = history.state?.editingRecord as ImmexRecord | undefined;
    if (record && !this.searchOnly) {
      this.editingId = record.id ?? null;
      this.form = { ...record };
    }
    this.load();
    if (!this.searchOnly) {
      this.loadCollections();
    }
  }

  get activeCount(): number {
    return this.records.filter((record) => record.activo).length;
  }

  get expiringSoonCount(): number {
    return this.records.filter((record) => this.daysLeft(record) <= 30).length;
  }

  get totalValue(): number {
    return this.records.reduce((sum, record) => sum + Number(record.cantidad || 0) * Number(record.valorUnitario || 0), 0);
  }

  load(): void {
    this.service.list().subscribe({
      next: (data) => {
        this.records = data;
        this.applyFilters();
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo cargar el inventario.';
      },
    });
  }

  loadCollections(): void {
    this.service.collections().subscribe({
      next: (data) => {
        this.collections = data;
        this.cdr.markForCheck();
      },
      error: () => {
        this.collections = [];
      },
    });
  }

  addPartida(): void {
    this.importFormModel.partidas.push({ material: '', cantidad: 1, valorUnitarioMxn: 0 });
  }

  removePartida(index: number): void {
    if (this.importFormModel.partidas.length === 1) return;
    this.importFormModel.partidas.splice(index, 1);
  }

  submitImport(): void {
    this.importError = '';
    const payload = {
      ...this.importFormModel,
      numeroPedimento: this.importFormModel.numeroPedimento.trim(),
      razonSocial: this.importFormModel.razonSocial.trim(),
      partidas: this.importFormModel.partidas.map((partida) => ({
        material: partida.material.trim(),
        cantidad: Number(partida.cantidad),
        valorUnitarioMxn: Number(partida.valorUnitarioMxn),
      })),
    };
    if (!payload.numeroPedimento || !payload.fecha || !payload.razonSocial || payload.partidas.some((p) => !p.material || !p.cantidad || !p.valorUnitarioMxn)) {
      this.importError = 'Completa los campos obligatorios de la captura.';
      return;
    }
    this.service.importPedimento(payload).subscribe({
      next: (result) => {
        this.importResult = result;
        this.resetImport();
        this.load();
        this.loadCollections();
      },
      error: () => {
        this.importError = 'No se pudo guardar la importación.';
      },
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'Todos';
    this.movementFilter = 'Todos';
    this.applyFilters();
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredRecords = this.records.filter((record) => {
      const matchesSearch = !term || [record.pedimento, record.material, record.proveedor, record.auditor ?? '']
        .join(' ')
        .toLowerCase()
        .includes(term);
      const matchesStatus = this.statusFilter === 'Todos' || record.status === this.statusFilter;
      const matchesMovement = this.movementFilter === 'Todos' || record.movimiento === this.movementFilter;
      return matchesSearch && matchesStatus && matchesMovement;
    });
  }

  save(): void {
    const payload = this.normalizeForm();
    const request$ = this.editingId ? this.service.update(this.editingId, payload) : this.service.create(payload);

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
    this.form = { ...record };
    if (this.searchOnly) {
      void this.router.navigateByUrl('/inventory', { state: { editingRecord: record } });
    }
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

  resetImport(): void {
    this.importFormModel = this.emptyImportForm();
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

  private emptyImportForm() {
    return {
      numeroPedimento: '',
      fecha: new Date().toISOString().slice(0, 10),
      razonSocial: '',
      partidas: [{ material: '', cantidad: 1, valorUnitarioMxn: 0 }],
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
