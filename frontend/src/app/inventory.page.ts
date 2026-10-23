import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ImmexService } from './immex.service';
import { CollectionSnapshot, ImmexRecord } from './models';

type ImportFormModel = {
  numeroPedimento: string;
  fecha: string;
  razonSocial: string;
  rfc: string;
  partidas: Array<{ partida: string; material: string; unidad: string; regimenAduanero: string; cantidad: number; valorUnitarioMxn: number }>;
};

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

      <section class="card case-card" *ngIf="!searchOnly">
        <div class="card-head">
          <h2>Casos de datos ficticios</h2>
          <span>Separados por planta y razón social</span>
        </div>
        <div class="case-grid">
          <article class="case-item" *ngFor="let item of demoCases">
            <div class="case-top">
              <div>
                <p class="case-kicker">{{ item.kicker }}</p>
                <h3>{{ item.title }}</h3>
              </div>
              <span class="case-pill">{{ item.total }} registros</span>
            </div>
            <p class="case-text">{{ item.description }}</p>
            <div class="case-stats">
              <span>Vigentes: {{ item.vigentes }}</span>
              <span>Por vencer: {{ item.porVencer }}</span>
              <span>Vencidos: {{ item.vencidos }}</span>
            </div>
            <div class="case-chips">
              <span class="badge" *ngFor="let company of item.companies">{{ company }}</span>
            </div>
            <ul class="case-list">
              <li *ngFor="let material of item.materials">{{ material }}</li>
            </ul>
          </article>
        </div>
      </section>

      <section class="card import-card" id="inventory-import-card" *ngIf="!searchOnly">
        <div class="import-card-title">
        <div class="card-head">
          <h2>Captura de pedimento de importación</h2>
          <span>Alta con partidas múltiples</span>
        </div>
        <button type="button" class="import-ai-launch" (click)="openAiDemo()"><span>✦</span> Probar captura asistida <b>→</b></button>
        </div>
        <div class="ai-draft-notice" *ngIf="draftApplied"><strong>Captura sugerida cargada.</strong> Revisa cada campo y confirma los datos antes de guardar.<button type="button" (click)="draftApplied = false" aria-label="Cerrar aviso">×</button></div>
        <p class="muted import-guidance">Captura los datos tal como aparecen en el pedimento. El RFC se completa al elegir la razón social.</p>

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
            <select [(ngModel)]="importFormModel.razonSocial" name="razonSocial" required (ngModelChange)="setImportCompany($event)">
              <option value="">Selecciona una razón social</option>
              <option *ngFor="let rs of razonSociales" [value]="rs.nombre">{{ rs.nombre }} · {{ rs.rfc }}</option>
            </select>
          </label>

          <label>
            <span>RFC</span>
            <input [(ngModel)]="importFormModel.rfc" name="rfc" required placeholder="Se completa al elegir razón social" />
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
                  <span>Partida</span>
                  <input [(ngModel)]="partida.partida" [name]="'partida' + i" required placeholder="01" />
                </label>
                <label>
                  <span>Material</span>
                  <input [(ngModel)]="partida.material" [name]="'material' + i" required placeholder="Resina ABS natural" />
                </label>
              </div>
              <div class="two-cols">
                <label>
                  <span>Unidad</span>
                  <input [(ngModel)]="partida.unidad" [name]="'unidad' + i" required placeholder="kg" />
                </label>
                <label>
                  <span>Régimen aduanero</span>
                  <input [(ngModel)]="partida.regimenAduanero" [name]="'regimenAduanero' + i" required placeholder="IMMEX temporal" />
                </label>
              </div>
              <div class="two-cols">
                <label>
                  <span>Cantidad</span>
                  <input [(ngModel)]="partida.cantidad" [name]="'cantidad' + i" type="number" min="1" required placeholder="1200" />
                </label>
                <label>
                  <span>Valor unitario en MXN</span>
                  <input [(ngModel)]="partida.valorUnitarioMxn" [name]="'valorUnitarioMxn' + i" type="number" step="0.01" min="0.01" required placeholder="3.20" />
                </label>
              </div>
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
              <span>Razón social</span>
              <select [(ngModel)]="form.razonSocial" name="razonSocial" required>
                <option value="">Selecciona una razón social</option>
                <option *ngFor="let option of razonSociales" [value]="option.nombre">{{ option.nombre }} · {{ option.rfc }}</option>
              </select>
            </label>
            <label>
              <span>RFC</span>
              <input [(ngModel)]="form.rfc" name="rfc" required placeholder="ASN9403159K2" />
            </label>
            <label>
              <span>Partida</span>
              <input [(ngModel)]="form.partida" name="partida" required placeholder="01" />
            </label>
            <label>
              <span>Unidad</span>
              <input [(ngModel)]="form.unidad" name="unidad" required placeholder="kg" />
            </label>
            <label>
              <span>Régimen aduanero</span>
              <input [(ngModel)]="form.regimenAduanero" name="regimenAduanero" required placeholder="IMMEX temporal" />
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
              <input [(ngModel)]="form.auditor" name="auditor" placeholder="Especialista aduanal" required />
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
                <input [(ngModel)]="form.valorUnitario" name="valorUnitario" type="number" step="0.01" min="0.01" placeholder="1.00" required />
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
              <textarea [(ngModel)]="form.notas" name="notas" rows="3" placeholder="Observaciones de conciliación" required></textarea>
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
                <th>Partida</th>
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
                  <div class="muted">{{ record.rfc }}</div>
                  <div class="muted">{{ record.proveedor }}</div>
                </td>
                <td>{{ record.partida || '—' }}</td>
                <td>
                  <strong>{{ record.material }}</strong>
                  <div class="muted" *ngIf="record.auditor">{{ record.auditor }}</div>
                  <div class="muted">{{ record.unidad || '—' }} · {{ record.regimenAduanero || '—' }}</div>
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
                <td colspan="9" class="empty">No hay registros para mostrar.</td>
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

  demoCases = [
    {
      kicker: 'Caso 1',
      title: 'Planta de inyección con merma y reciclado',
      description: 'Resina ABS y regranulado con lotes frescos, material por vencer y un lote vencido para simular el cierre de merma y retorno interno.',
      total: 3,
      vigentes: 1,
      porVencer: 1,
      vencidos: 1,
      companies: ['Aerosistemas del Norte, S.A. de C.V.'],
      materials: ['Resina ABS natural', 'Regranulado ABS reciclado', 'Pigmento masterbatch negro'],
    },
    {
      kicker: 'Caso 2',
      title: 'Planta de arneses con BOM amplio',
      description: 'Arnés automotriz con múltiples componentes: conectores, terminales, cinta, grommets y consumibles, con inventario cruzado entre estados.',
      total: 3,
      vigentes: 1,
      porVencer: 1,
      vencidos: 1,
      companies: ['Componentes Industriales del Bajío, S.A. de C.V.'],
      materials: ['Conector 12 pines', 'Terminal hembra 6.3 mm', 'Cinta PVC automotriz'],
    },
    {
      kicker: 'Caso 3',
      title: 'Empresa con 2 razones sociales',
      description: 'Vista compartida de dos empresas para validar que el selector global mantiene la separación y permite revisar ambas razones sociales sin mezclar saldos.',
      total: 6,
      vigentes: 2,
      porVencer: 2,
      vencidos: 2,
      companies: ['Aerosistemas del Norte, S.A. de C.V.', 'Componentes Industriales del Bajío, S.A. de C.V.'],
      materials: ['Inventario por empresa separado', 'Lotes vigentes / por vencer / vencidos', 'Consulta consolidada verificada'],
    },
  ];
  records: ImmexRecord[] = [];
  filteredRecords: ImmexRecord[] = [];
  collections: CollectionSnapshot[] = [];
  editingId: string | null = null;
  error = '';
  importError = '';
  importResult: any = null;
  draftApplied = false;
  searchTerm = '';
  statusFilter = 'Todos';
  movementFilter = 'Todos';
  razonSociales = [
    { nombre: 'Aerosistemas del Norte, S.A. de C.V.', rfc: 'ASN9403159K2' },
    { nombre: 'Componentes Industriales del Bajío, S.A. de C.V.', rfc: 'CIB0209187Q4' },
  ];
  importFormModel = this.emptyImportForm();
  form: ImmexRecord = this.emptyForm();

  ngOnInit(): void {
    const record = history.state?.editingRecord as ImmexRecord | undefined;
    const draft = history.state?.draftPedimento as ImportFormModel | undefined;
    if (record && !this.searchOnly) {
      this.editingId = record.id ?? null;
      this.form = { ...record };
    }
    if (draft && !this.searchOnly) {
      this.importFormModel = { ...this.emptyImportForm(), ...draft, partidas: draft.partidas.map((partida) => ({ ...partida })) };
      this.draftApplied = true;
      window.setTimeout(() => document.getElementById('inventory-import-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 100);
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
    this.importFormModel.partidas.push({ partida: '', material: '', unidad: '', regimenAduanero: '', cantidad: 1, valorUnitarioMxn: 0 });
  }

  removePartida(index: number): void {
    if (this.importFormModel.partidas.length === 1) return;
    this.importFormModel.partidas.splice(index, 1);
  }

  submitImport(): void {
    this.importError = '';
    const payload = {
      numeroPedimento: this.importFormModel.numeroPedimento.trim(),
      fecha: this.importFormModel.fecha,
      razonSocial: this.importFormModel.razonSocial.trim(),
      rfc: this.importFormModel.rfc.trim(),
      partidas: this.importFormModel.partidas.map((partida) => ({
        partida: partida.partida.trim(),
        material: partida.material.trim(),
        unidad: partida.unidad.trim(),
        regimenAduanero: partida.regimenAduanero.trim(),
        cantidad: Number(partida.cantidad),
        valorUnitarioMxn: Number(partida.valorUnitarioMxn),
      })),
    };
    if (!payload.numeroPedimento || !payload.fecha || !payload.razonSocial || !payload.rfc || payload.partidas.some((p) => !p.partida || !p.material || !p.unidad || !p.regimenAduanero || !p.cantidad || !p.valorUnitarioMxn)) {
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
      error: (err: unknown) => {
        this.importError = this.errorMessage(err, 'No se pudo guardar la importación.');
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
      const matchesSearch = !term || [record.pedimento, record.material, record.proveedor, record.auditor ?? '', record.rfc ?? '', record.partida ?? '']
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
      error: (err: unknown) => {
        this.error = this.errorMessage(err, 'No se pudo guardar el registro.');
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
    this.draftApplied = false;
  }

  setImportCompany(razonSocial: string): void {
    this.importFormModel.rfc = this.razonSociales.find((option) => option.nombre === razonSocial)?.rfc ?? '';
  }

  openAiDemo(): void {
    void this.router.navigateByUrl('/assistant');
  }

  daysLeft(record: ImmexRecord): number {
    const due = new Date(record.fechaVencimiento + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return Math.ceil((due.getTime() - today.getTime()) / 86400000);
  }

  formatDate(value: string): string {
    const date = new Date(value + 'T00:00:00');
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
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
      valorUnitario: '' as any,
      fechaIngreso: isoToday,
      fechaVencimiento: due.toISOString().slice(0, 10),
      status: 'Vigente',
      activo: true,
      notas: '',
      auditor: 'Sistema',
      razonSocial: '',
      rfc: '',
      partida: '',
      unidad: '',
      regimenAduanero: '',
    };
  }

  private emptyImportForm() {
    return {
      numeroPedimento: '',
      fecha: new Date().toISOString().slice(0, 10),
      razonSocial: '',
      rfc: '',
      partidas: [{ partida: '', material: '', unidad: '', regimenAduanero: '', cantidad: 1, valorUnitarioMxn: 0 }],
    };
  }

  private errorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const detail = typeof error.error === 'string'
        ? error.error
        : error.error?.message || error.error?.detail || error.error?.error;
      if (typeof detail === 'string' && detail.trim()) return `${fallback} ${detail}`;
    }
    return fallback;
  }

  private normalizeForm(): ImmexRecord {
    return {
      pedimento: this.form.pedimento.trim(),
      material: this.form.material.trim(),
      proveedor: this.form.proveedor.trim(),
      movimiento: this.form.movimiento,
      razonSocial: (this.form.razonSocial ?? '').trim(),
      rfc: (this.form.rfc ?? '').trim(),
      partida: (this.form.partida ?? '').trim(),
      unidad: (this.form.unidad ?? '').trim(),
      regimenAduanero: (this.form.regimenAduanero ?? '').trim(),
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
