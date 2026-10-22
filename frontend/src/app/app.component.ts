import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { ImmexService } from './immex.service';
import { DashboardInventorySummary, ImmexCloseReport, ImmexDashboardSummary, ImmexRecord, PepsOperationResult, ProduccionMensualLine, ProduccionMensualResult, Anexo24Query, RazonSocialOption, LoteMaterialView, CloseMonthlyStateResponse, CloseMonthlyHistoryItem, CloseMonthlyStatus } from './models';

type DashboardView = 'dashboard' | 'movement' | 'ledger' | 'flow' | 'summary' | 'risk' | 'peps' | 'production' | 'export' | 'close' | 'audit';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="shell">
      <section class="alert-banner" *ngIf="error" role="alert" aria-live="assertive">
        <strong>Atención</strong>
        <p>{{ error }}</p>
      </section>
      <ng-container [ngSwitch]="activeView">
        <section *ngSwitchCase="'dashboard'" class="dashboard-panel">
          <header class="card hero-summary" id="dashboard">
            <div>
              <p class="eyebrow">IMMEX · Operación</p>
              <h1>Panel general</h1>
              <p class="subtitle">Resumen operativo del sistema IMMEX con indicadores activos y acceso rápido a las secciones.</p>
            </div>
            <div class="summary-inline">
              <div><span>Saldo total</span><strong>{{ inventorySaldoTotal | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
              <div><span>Monto en riesgo</span><strong>{{ inventoryMontoEnRiesgo | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
              <div><span>Importaciones</span><strong>{{ entryCount }}</strong></div>
              <div><span>Exportaciones</span><strong>{{ exportCount }}</strong></div>
              <div><span>Por vencer 90 días</span><strong>{{ inventoryMontoPorVencer | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
              <div><span>Vencido</span><strong>{{ inventoryMontoVencido | currency:'MXN':'symbol':'1.0-0' }}</strong></div>
              <div><span>Cierre mensual</span><strong>{{ signedCloseStatus }}</strong><small>{{ closeSignedAt }}</small></div>
            </div>
          </header>

          <section class="card risk-card" id="risk-section">
            <div class="card-head">
              <h2>Materiales por urgencia</h2>
              <span>{{ inventoryRows.length }} materiales</span>
            </div>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Material</th>
                    <th>Pedimento</th>
                    <th>RFC</th>
                    <th>Saldo pendiente</th>
                    <th>Fecha límite</th>
                    <th>Días</th>
                    <th>Valor mercancía</th>
                    <th>Exposición MXN</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let row of taxExposureRows">
                    <td><strong>{{ row.material }}</strong></td>
                    <td>{{ row.pedimento }}</td>
                    <td>{{ row.rfc }}</td>
                    <td>{{ row.saldoAduaneroPendiente }}</td>
                    <td>{{ row.fechaLimite }}</td>
                    <td><span class="badge" [class.warning]="row.diasRestantes <= 90" [class.danger]="row.estado === 'vencido'">{{ row.estado }} · {{ row.diasRestantes }} días</span></td>
                    <td>{{ row.valorMercancia }}</td>
                    <td>{{ row.impuestosPotenciales }}</td>
                  </tr>
                  <tr *ngIf="!taxExposureRows.length">
                    <td colspan="8" class="empty">No hay materiales con saldo disponible.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="card module-card" id="exposicion-section">
            <div class="card-head">
              <h2>Exposición económica</h2>
              <span>Valor mercancía y saldos</span>
            </div>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Material</th>
                    <th>Pedimento</th>
                    <th>RFC</th>
                    <th>Saldo aduanero</th>
                    <th>Valor mercancía</th>
                    <th>Impuestos potenciales</th>
                    <th>Fecha límite</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let row of taxExposureRows">
                    <td>{{ row.material }}</td>
                    <td>{{ row.pedimento }}</td>
                    <td>{{ row.rfc }}</td>
                    <td>{{ row.saldoAduaneroPendiente }}</td>
                    <td>{{ row.valorMercancia }}</td>
                    <td>{{ row.impuestosPotenciales }}</td>
                    <td>{{ row.fechaLimite }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
          <section class="card module-card" id="anexo24-section">
            <div class="card-head">
              <h2>Anexo 24</h2>
              <span>Excel por lote</span>
            </div>
            <div class="form compact-form">
              <div class="two-cols">
                <label>
                  <span>Razón social</span>
                  <input [(ngModel)]="anexo24Filters.razonSocial" name="anexo24RazonSocial" placeholder="Aerosistemas del Norte, S.A. de C.V." />
                </label>
                <label>
                  <span>Mes</span>
                  <input [(ngModel)]="anexo24Filters.mes" name="anexo24Mes" type="month" />
                </label>
              </div>
              <div class="actions">
                <button class="primary" type="button" (click)="downloadAnexo24()">Descargar reporte</button>
              </div>
            </div>
          </section>
        </section>

        <section *ngSwitchCase="'movement'" class="card form-card" id="form-card">
          <div class="card-head">
            <h2>Captura de movimiento</h2>
            <span>Alta y edición del registro</span>
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
        </section>

        <section *ngSwitchCase="'ledger'" class="card list-card" id="list-card">
          <div class="card-head">
            <h2>Ledger IMMEX</h2>
            <span>{{ ledgerRows.length }} movimientos</span>
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
                <tr *ngFor="let record of ledgerRows">
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
                <tr *ngIf="!ledgerRows.length">
                  <td colspan="9" class="empty">Aún no hay movimientos.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section *ngSwitchCase="'flow'" class="card module-card" id="flow-section">
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
        </section>

        <section *ngSwitchCase="'summary'" class="card module-card" id="summary-section">
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
        </section>

        <section *ngSwitchCase="'risk'" class="card module-card wide-card" id="risk-section">
          <div class="card-head">
            <h2>Excepciones y riesgo</h2>
            <span>{{ expiringSoonCount }} urgentes</span>
          </div>
          <div class="exception-list">
            <div class="exception-item" *ngFor="let record of riskAlerts">
              <div class="exception-head">
                <strong>{{ record.material }}</strong>
                <button type="button" class="ghost" (click)="explainException(record)">Explicar</button>
              </div>
              <span>{{ record.pedimento }} · {{ daysLeft(record) }} días · {{ riskFor(record) | currency:'MXN':'symbol':'1.0-0' }}</span>
              <p class="muted" *ngIf="getExceptionExplanation(record)">{{ getExceptionExplanation(record) }}</p>
            </div>
            <div class="empty" *ngIf="!riskAlerts.length">Sin excepciones por ahora.</div>
          </div>
        </section>

        <section *ngSwitchCase="'peps'" class="card module-card" id="peps-section">
          <div class="card-head">
            <h2>PEPS</h2>
            <span>Simulador y ejecución</span>
          </div>
          <form class="form compact-form" (ngSubmit)="simulatePeps()">
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
                <input [(ngModel)]="pepsForm.auditor" name="pepsAuditor" placeholder="Especialista" required />
              </label>
            </div>
            <label>
              <span>Motivo</span>
              <textarea [(ngModel)]="pepsForm.reason" name="pepsReason" rows="2" placeholder="Salida para producción" required></textarea>
            </label>
            <div class="actions">
              <button class="primary" type="submit">Simular</button>
              <button class="ghost" type="button" (click)="applyPeps()" [disabled]="!pepsPreview">Aplicar</button>
              <button class="ghost" type="button" (click)="revertPeps()" [disabled]="!pepsApplied">Revertir</button>
            </div>
          </form>
          <div class="close-report-box" *ngIf="pepsPreview; else pepsEmpty">
            <p><strong>Material:</strong> {{ pepsPreview.material }}</p>
            <p><strong>Solicitado:</strong> {{ pepsPreview.requestedQuantity }}</p>
            <p><strong>Procesado:</strong> {{ pepsPreview.processedQuantity }}</p>
            <p><strong>Estado:</strong> {{ pepsPreview.completed ? 'Completo' : 'Parcial' }}</p>
            <p><strong>Mensaje:</strong> {{ pepsPreview.message }}</p>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Lote</th>
                    <th>Pedimento</th>
                    <th>Material</th>
                    <th>Cantidad</th>
                    <th>Saldo antes</th>
                    <th>Saldo después</th>
                    <th>Tipo</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let item of pepsPreview.movimientos">
                    <td>{{ item.loteId }}</td>
                    <td>{{ item.pedimento }}</td>
                    <td>{{ item.material }}</td>
                    <td>{{ item.cantidad }}</td>
                    <td>{{ item.saldoAntes }}</td>
                    <td>{{ item.saldoDespues }}</td>
                    <td>{{ item.tipoMovimiento }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <ng-template #pepsEmpty>
            <p class="muted">Simula una descarga para revisar cómo quedarían los saldos antes de aplicar.</p>
          </ng-template>
          <p class="error" *ngIf="pepsError" role="alert">{{ pepsError }}</p>
          <p class="success" *ngIf="pepsResult">{{ pepsResult.message }}</p>
        </section>

        <section *ngSwitchCase="'production'" class="card module-card" id="production-section">
          <div class="card-head">
            <h2>Producción mensual</h2>
            <span>Consumo por BOM</span>
          </div>

          <form class="form" (ngSubmit)="previewProduction()">
            <div class="two-cols">
              <label>
                <span>Producto</span>
                <select [(ngModel)]="productionForm.producto" name="prodProducto" required>
                  <option value="">Selecciona un producto</option>
                  <option *ngFor="let product of productionProducts" [value]="product">{{ product }}</option>
                </select>
              </label>
              <label>
                <span>Cantidad producida</span>
                <input [(ngModel)]="productionForm.cantidadProducida" name="prodCantidad" type="number" min="1" required />
              </label>
            </div>
            <div class="two-cols">
              <label>
                <span>Merma %</span>
                <input [(ngModel)]="productionForm.mermaPct" name="prodMerma" type="number" min="0" step="0.01" required />
              </label>
              <label>
                <span>Responsable</span>
                <input [(ngModel)]="productionForm.auditor" name="prodAuditor" placeholder="Supervisor" required />
              </label>
            </div>
            <label>
              <span>Observación</span>
              <textarea [(ngModel)]="productionForm.observacion" name="prodObservacion" rows="2" placeholder="Cierre mensual / orden de producción" required></textarea>
            </label>
            <div class="actions">
              <button class="primary" type="button" (click)="previewProduction()">Previsualizar consumo</button>
              <button class="ghost" type="button" (click)="executeProduction()" [disabled]="!productionPreview">Confirmar producción</button>
              <button class="ghost" type="button" (click)="downloadAnexo24()">Descargar reporte</button>
            </div>
          </form>

          <div class="close-report-box" *ngIf="productionPreview; else productionEmpty">
            <p><strong>Producto:</strong> {{ productionPreview.producto }}</p>
            <p><strong>Cantidad:</strong> {{ productionPreview.cantidadProducida }}</p>
            <p><strong>Merma:</strong> {{ productionPreview.mermaPct }}%</p>
            <p><strong>Movimientos:</strong> {{ productionPreview.movimientosDescarga }}</p>
            <p><strong>Estado:</strong> {{ productionPreview.completado ? 'Listo' : 'Pendiente' }}</p>
            <p class="error" *ngIf="!productionPreview.completado" role="alert">Falta saldo para completar la producción; no se descontará nada hasta confirmar.</p>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Material</th>
                    <th>Cantidad por BOM</th>
                    <th>Merma incluida</th>
                    <th>Lote de origen (PEPS)</th>
                    <th>Saldo pendiente</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let line of productionPreview.lineas">
                    <td>{{ line.material }}</td>
                    <td>{{ line.cantidadBom }}</td>
                    <td>{{ line.mermaIncluida }}</td>
                    <td>{{ line.lotesAfectados.join(', ') || '-' }}</td>
                    <td [class.danger-text]="line.saldoPendiente > 0">{{ line.saldoPendiente }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <ng-template #productionEmpty>
            <p class="muted">Previsualiza la producción para ver el consumo antes de confirmar.</p>
          </ng-template>

          <p class="error" *ngIf="productionError" role="alert">{{ productionError }}</p>
          <p class="success" *ngIf="productionResult">{{ productionResult.message }}</p>
        </section>

        <section *ngSwitchCase="'close'" class="card module-card" id="close-section">
          <div class="card-head">
            <h2>Cierre mensual</h2>
            <span>Reporte listo</span>
          </div>
          <div class="close-step-list" *ngIf="closeState?.status?.steps?.length">
            <div class="close-step" *ngFor="let step of closeState.status.steps">
              <div class="close-step-head">
                <strong>{{ step.label }}</strong>
                <span class="badge" [class.warning]="step.estado !== 'Completada'">{{ step.estado }}</span>
              </div>
              <p>{{ step.responsable }}</p>
              <p class="muted">{{ step.pendiente }}</p>
            </div>
          </div>
          <div class="close-step-list" *ngIf="closeState?.status?.history?.length">
            <div class="close-step" *ngFor="let item of closeState.status.history">
              <div class="close-step-head">
                <strong>{{ item.stepLabel }}</strong>
                <span>{{ item.changedAt | date:'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <p>{{ item.actor }} · {{ item.field }}</p>
              <p class="muted">{{ item.oldValue }} → {{ item.newValue }} · {{ item.reason }}</p>
            </div>
          </div>
          <div class="close-step-list" *ngIf="closeHistory.length && !closeState?.status?.history?.length">
            <div class="close-step" *ngFor="let item of closeHistory">
              <div class="close-step-head">
                <strong>{{ item.periodo }}</strong>
                <span>{{ item.firmado ? 'Firmado' : 'Abierto' }}</span>
              </div>
              <p>Avance {{ item.progressPct }}% · Pendientes {{ item.pendingCount }}</p>
            </div>
          </div>
          <div class="close-report-box" *ngIf="closeState; else closeReportEmpty">
            <p><strong>Avance:</strong> {{ closeState.status.progressPct }}%</p>
            <p><strong>Estado:</strong> {{ closeState.status.firmado ? 'Firmado' : (closeState.status.canClose ? 'Listo para aprobar' : 'En proceso') }}</p>
            <p><strong>Periodo:</strong> {{ closeState.status.periodo }}</p>
            <p><strong>Responsable actual:</strong> {{ closeState.status.currentResponsible }}</p>
            <p *ngIf="closeState.status.firmado"><strong>Firmado por:</strong> {{ closeState.status.firmadoPor || '—' }}</p>
            <p *ngIf="closeState.status.firmadoAt"><strong>Firma:</strong> {{ closeState.status.firmadoAt | date:'dd/MM/yyyy HH:mm' }}</p>
            <p><strong>Pendientes:</strong> {{ closeState.status.pendingCount }}</p>
            <p><strong>Errores críticos:</strong> {{ closeState.status.hasCriticalErrors ? 'Sí' : 'No' }}</p>
            <p><strong>Saldo total:</strong> {{ closeReport?.saldoTotalMxn | currency:'MXN':'symbol':'1.0-0' }}</p>
            <p><strong>Monto en riesgo:</strong> {{ closeReport?.montoEnRiesgoMxn | currency:'MXN':'symbol':'1.0-0' }}</p>
            <p><strong>Abiertos:</strong> {{ closeReport?.openRecords ?? 0 }}</p>
            <p><strong>Por vencer:</strong> {{ closeReport?.expiringSoonCount ?? 0 }}</p>
            <p *ngIf="closeReport?.pepsCandidate"><strong>PEPS:</strong> {{ closeReport?.pepsCandidate?.material }}</p>
            <div class="actions">
              <button class="primary" type="button" *ngIf="closeState.canSign" (click)="signClose()">Firmar cierre</button>
              <button class="ghost" type="button" (click)="downloadAnexo24()" [disabled]="!closeState.canDownloadAnexo24">Descargar reporte Anexo 24</button>
            </div>
          </div>
          <ng-template #closeReportEmpty>
            <p class="muted">Sin reporte aún.</p>
          </ng-template>
          <button class="ghost" type="button" (click)="refreshCloseState()">Actualizar cierre</button>
        </section>

        <section *ngSwitchCase="'audit'" class="card audit-card" id="audit-section">
          <div class="card-head">
            <h2>Trazabilidad</h2>
            <span>quién, cuándo y por qué</span>
          </div>
          <div class="assistant-box" *ngIf="assistantData; else assistantLoading">
            <p class="eyebrow">Asistente inteligente de cumplimiento</p>
            <h3>{{ assistantData.summary }}</h3>
            <p class="muted">Ámbito: {{ assistantData.scope }}</p>
            <div class="assistant-list" *ngIf="assistantData.recommendations.length; else noAssistantItems">
              <article class="assistant-item" *ngFor="let item of assistantData.recommendations">
                <strong>{{ item.problem }}</strong>
                <p><b>Causa:</b> {{ item.cause }}</p>
                <p><b>Impacto:</b> {{ item.impact }}</p>
                <p><b>Acción sugerida:</b> {{ item.suggestedAction }}</p>
                <p><b>Evidencia:</b> {{ item.evidence }}</p>
                <small>{{ item.factBased ? 'Hecho real' : 'Estimación' }}</small>
              </article>
            </div>
            <ng-template #noAssistantItems>
              <p class="muted">No se detectaron recomendaciones.</p>
            </ng-template>
            <div class="assistant-list" *ngIf="assistantData.facts.length">
              <article class="assistant-item">
                <strong>Hechos observados</strong>
                <ul>
                  <li *ngFor="let fact of assistantData.facts">{{ fact }}</li>
                </ul>
              </article>
            </div>
            <div class="assistant-list" *ngIf="assistantData.limitations.length">
              <article class="assistant-item">
                <strong>Limitaciones</strong>
                <ul>
                  <li *ngFor="let limitation of assistantData.limitations">{{ limitation }}</li>
                </ul>
              </article>
            </div>
          </div>
          <ng-template #assistantLoading>
            <p class="muted">Cargando asistente de cumplimiento…</p>
          </ng-template>
          <button class="ghost" type="button" (click)="refreshAssistant()">Actualizar asistente</button>
          <div class="audit-list" *ngIf="auditEvents.length; else auditEmpty">
            <div class="audit-item" *ngFor="let event of auditEvents">
              <strong>{{ event.action }} · {{ event.entityType }}</strong>
                <span>{{ event.actor || '—' }} · {{ event.reason || 'Motivo no informado' }} · {{ event.createdAt ? (event.createdAt | date:'dd/MM/yyyy HH:mm') : '—' }}</span>
              <p>{{ event.details }}</p>
            </div>
          </div>
          <ng-template #auditEmpty>
            <p class="muted">Sin eventos de auditoría todavía.</p>
          </ng-template>
          <button class="ghost" type="button" (click)="refreshAudit()">Actualizar trazabilidad</button>
        </section>
      </ng-container>
    </main>
  `,
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {
  private readonly service = inject(ImmexService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private routerSubscription?: Subscription;

  activeView: DashboardView = 'dashboard';
  records: ImmexRecord[] = [];
  lotes: LoteMaterialView[] = [];
  summary: ImmexDashboardSummary | null = null;
  inventorySummary: DashboardInventorySummary | null = null;
  closeReport: ImmexCloseReport | null = null;
  closeState: CloseMonthlyStateResponse | null = null;
  closeHistory: CloseMonthlyHistoryItem[] = [];
  assistantData: { scope: string; summary: string; recommendations: { problem: string; cause: string; impact: string; suggestedAction: string; evidence: string; factBased: boolean }[]; facts: string[]; limitations: string[] } | null = null;
  pepsPreview: PepsOperationResult | null = null;
  pepsResult: PepsOperationResult | null = null;
  auditEvents: any[] = [];
  productionPreview: ProduccionMensualResult | null = null;
  productionResult: ProduccionMensualResult | null = null;
  productionError = '';
  pepsError = '';
  pepsApplied = false;
  editingId: string | null = null;
  connected = true;
  error = '';
  exceptionExplanations: Record<string, string> = {};
  form: ImmexRecord = this.emptyForm();
  pepsForm = {
    material: 'Resina ABS',
    quantity: 100,
    reason: 'Consumo para producción',
    auditor: 'Especialista',
  };
  productionProducts = ['Arnés eléctrico automotriz', 'Carcasa de control'];
  productionForm = {
    producto: 'Arnés eléctrico automotriz',
    cantidadProducida: 100,
    mermaPct: 2,
    auditor: 'Supervisor',
    observacion: 'Producción mensual',
  };
  anexo24Filters = {
    razonSocial: '',
    mes: '',
  };
  razonSociales: RazonSocialOption[] = [
    { nombre: 'Aerosistemas del Norte, S.A. de C.V.', rfc: 'ASN9403159K2', programaImmex: 'IMMEX Manufactura', activo: true },
    { nombre: 'Componentes Industriales del Bajío, S.A. de C.V.', rfc: 'CIB0209187Q4', programaImmex: 'IMMEX Servicios', activo: true },
  ];

  ngOnInit(): void {
    this.syncView();
    const editingRecord = history.state?.editingRecord as ImmexRecord | undefined;
    if (editingRecord && this.activeView === 'movement') {
      this.editingId = editingRecord.id ?? null;
      this.form = { ...editingRecord };
    }
    this.routerSubscription = this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(() => this.syncView());
    this.load();
    this.loadInventorySummary();
    this.refreshCloseReport();
    this.refreshCloseState();
    this.refreshAssistant();
    this.refreshAudit();
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe();
  }

  get riskTotal(): number {
    return this.summary?.riskTotal ?? this.records.reduce((sum, record) => sum + this.riskFor(record), 0);
  }

  get ledgerRows(): ImmexRecord[] {
    return this.lotes.length ? this.lotes.map((row) => this.toRecord(row)) : this.records;
  }

  get inventoryRows() {
    return [...(this.inventorySummary?.materiales ?? [])].sort((a, b) => {
      const aRank = this.urgencyRank(a.diasRestantes);
      const bRank = this.urgencyRank(b.diasRestantes);
      return aRank - bRank || a.diasRestantes - b.diasRestantes;
    });
  }

  get inventorySaldoTotal(): number {
    return this.inventorySummary?.saldoTotalMxn ?? 0;
  }

  get inventoryMontoVencido(): number {
    return this.inventorySummary?.montoVencidoMxn ?? 0;
  }

  get inventoryMontoPorVencer(): number {
    return this.inventorySummary?.montoPorVencerMxn ?? 0;
  }

  get inventoryMontoEnRiesgo(): number {
    return (this.inventorySummary?.montoVencidoMxn ?? 0) + (this.inventorySummary?.montoPorVencerMxn ?? 0);
  }



  get inventoryTopRows() {
    return this.inventoryRows.slice(0, 5);
  }

  get signedCloseStatus(): string {
    return this.closeState?.status?.firmado ? 'Firmado' : (this.closeReport?.estado || 'Abierto');
  }

  get closeSignedAt(): string {
    return this.closeState?.status?.firmadoAt ? this.formatDateTime(this.closeState.status.firmadoAt) : 'No calculable';
  }

  get taxExposureRows() {
    return this.inventoryRows.slice(0, 8).map((row) => ({
      material: row.material,
      pedimento: row.pedimento,
      rfc: row.rfc || 'No calculable',
      valorMercancia: row.saldoAduaneroPendiente > 0 ? this.moneyLabel(row.montoEnRiesgo) : 'No calculable',
      impuestosPotenciales: 'No calculable',
      saldoAduaneroPendiente: row.saldoAduaneroPendiente > 0 ? this.moneyLabel(row.saldoAduaneroPendiente) : 'No calculable',
      fechaLimite: row.fechaVencimiento ? this.formatDate(row.fechaVencimiento) : 'No calculable',
      diasRestantes: row.diasRestantes,
      estado: row.estado,
    }));
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

  private urgencyRank(diasRestantes: number): number {
    if (diasRestantes < 0) return 0;
    if (diasRestantes <= 30) return 1;
    if (diasRestantes <= 60) return 2;
    if (diasRestantes <= 90) return 3;
    return 4;
  }

  private moneyLabel(value: number): string {
    return Number.isFinite(value) ? value.toLocaleString('es-MX', { style: 'currency', currency: 'MXN' }) : 'No calculable';
  }

  private formatDateTime(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return 'No calculable';
    return date.toLocaleString('es-MX', { dateStyle: 'short', timeStyle: 'short' });
  }

  get riskAlerts(): ImmexRecord[] {
    return this.ledgerRows
      .filter((record) => this.isRiskRelevant(record) && (this.daysLeft(record) <= 60 || record.status === 'Conciliación'))
      .sort((a: ImmexRecord, b: ImmexRecord) => {
        const aPriority = a.status === 'Conciliación' ? 0 : 1;
        const bPriority = b.status === 'Conciliación' ? 0 : 1;
        return aPriority - bPriority || this.daysLeft(a) - this.daysLeft(b);
      });
  }

  get conector12Rows(): LoteMaterialView[] {
    return this.getLoteViews('Conector 12 pines');
  }

  get conector12First(): LoteMaterialView | null {
    return this.conector12Rows[0] ?? null;
  }

  get conector12DisplayDays(): string {
    const row = this.conector12First;
    return row ? `${row.diasRestantes} días` : '—';
  }

  get conector12DisplayDate(): string {
    const row = this.conector12First;
    return row?.fechaVencimiento ? this.formatDate(row.fechaVencimiento) : '—';
  }

  getLoteViews(material?: string): LoteMaterialView[] {
    return this.lotes.filter((row) => !material || row.material.toLowerCase().includes(material.toLowerCase()));
  }

  load(): void {
    this.service.list().subscribe({
      next: (data) => {
        this.records = data;
        this.connected = true;
        this.error = '';
        this.loadSummary();
        this.loadLotes();
        this.cdr.markForCheck();
      },
      error: () => {
        this.connected = false;
        this.cdr.markForCheck();
        this.error = 'No se pudo conectar al backend.';
      },
    });
  }

  loadSummary(): void {
    this.service.summary().subscribe({
      next: (data) => { this.summary = data; this.cdr.markForCheck(); },
      error: () => (this.summary = null),
    });
  }

  loadInventorySummary(): void {
    this.service.inventorySummary().subscribe({
      next: (data) => { this.inventorySummary = data; this.cdr.markForCheck(); },
      error: () => (this.inventorySummary = null),
    });
  }

  loadLotes(): void {
    this.service.lotes(undefined, this.service.getCompanyFilter()).subscribe({
      next: (data) => { this.lotes = data; this.cdr.markForCheck(); },
      error: () => (this.lotes = []),
    });
  }

  refreshCloseReport(): void {
    this.service.closeReport().subscribe({
      next: (data) => { this.closeReport = data; this.cdr.markForCheck(); },
      error: () => (this.closeReport = null),
    });
  }

  refreshCloseState(): void {
    this.service.closeStatus().subscribe({
      next: (data) => { this.closeState = data; this.cdr.markForCheck(); },
      error: () => (this.closeState = null),
    });
    this.service.closeHistory().subscribe({
      next: (data) => { this.closeHistory = data; this.cdr.markForCheck(); },
      error: () => (this.closeHistory = []),
    });
  }

  refreshAssistant(): void {
    this.service.complianceAssistant().subscribe({
      next: (data) => { this.assistantData = data; this.cdr.markForCheck(); },
      error: () => (this.assistantData = null),
    });
  }

  refreshAudit(): void {
    this.service.auditEvents().subscribe({
      next: (data) => { this.auditEvents = data; this.cdr.markForCheck(); },
      error: () => (this.auditEvents = []),
    });
  }

  signClose(): void {
    const actor = (this.productionForm.auditor || this.pepsForm.auditor || 'Especialista').trim();
    this.service.signClose({ actor }).subscribe({
      next: (data) => {
        this.closeState = {
          status: data,
          canSign: false,
          canDownloadAnexo24: true,
        };
        this.refreshCloseReport();
        this.refreshCloseState();
        this.cdr.markForCheck();
      },
      error: (err: unknown) => {
        this.error = this.extractHttpMessage(err, 'No se pudo firmar el cierre mensual.');
      },
    });
  }

  previewProduction(): void {
    this.productionError = '';
    this.service.productionPreview(this.productionForm).subscribe({
      next: (data) => {
        this.productionPreview = data;
        this.productionResult = null;
        this.cdr.markForCheck();
      },
      error: (err: unknown) => {
        this.productionError = 'Producción: ' + this.extractHttpMessage(err, 'No se pudo previsualizar la producción.');
      },
    });
  }

  downloadAnexo24(): void {
    if (!this.closeState?.canDownloadAnexo24) {
      this.error = 'Debes firmar el cierre antes de descargar el Anexo 24.';
      return;
    }
    const query: Anexo24Query = {
      razonSocial: this.anexo24Filters.razonSocial?.trim() || undefined,
      mes: this.anexo24Filters.mes?.trim() || undefined,
    };
    this.service.downloadAnexo24(query).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `anexo24${query.mes ? `-${query.mes}` : ''}.xlsx`;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: (err: unknown) => {
        this.error = this.extractHttpMessage(err, 'No se pudo descargar el reporte Anexo 24.');
      },
    });
  }

  executeProduction(): void {
    if (!this.productionPreview) {
      this.productionError = 'Primero genera la previsualización.';
      return;
    }
    this.productionError = '';
    this.service.productionExecute(this.productionForm).subscribe({
      next: (data) => {
        this.productionResult = data;
        this.productionPreview = data;
        this.cdr.markForCheck();
        this.load();
        this.loadInventorySummary();
        this.refreshCloseReport();
      },
      error: (err: unknown) => {
        this.productionError = 'Producción: ' + this.extractHttpMessage(err, 'No se pudo ejecutar la producción mensual.');
      },
    });
  }

  simulatePeps(): void {
    this.service.simulatePeps(this.pepsForm).subscribe({
      next: (data) => {
        this.pepsPreview = data;
        this.pepsResult = null;
        this.pepsApplied = false;
        this.cdr.markForCheck();
      },
      error: (err: unknown) => {
        this.pepsError = 'PEPS: ' + this.extractHttpMessage(err, 'No se pudo simular PEPS.');
      },
    });
  }

  applyPeps(): void {
    if (!this.pepsPreview) {
      this.error = 'Primero simula la descarga.';
      return;
    }
    this.service.applyPeps(this.pepsForm).subscribe({
      next: (data) => {
        this.pepsResult = data;
        this.pepsPreview = data;
        this.pepsApplied = true;
        this.cdr.markForCheck();
        this.load();
        this.refreshCloseReport();
      },
      error: (err: unknown) => {
        this.pepsError = 'PEPS: ' + this.extractHttpMessage(err, 'No se pudo aplicar PEPS.');
      },
    });
  }

  revertPeps(): void {
    if (!this.pepsPreview) {
      this.error = 'Primero simula la descarga.';
      return;
    }
    this.service.revertPeps(this.pepsForm).subscribe({
      next: (data) => {
        this.pepsResult = data;
        this.pepsPreview = null;
        this.pepsApplied = false;
        this.cdr.markForCheck();
      },
      error: (err: unknown) => {
        this.pepsError = 'PEPS: ' + this.extractHttpMessage(err, 'No se pudo revertir la simulación.');
      },
    });
  }

  goToCloseItem(href: string): void {
    void this.router.navigateByUrl(href);
  }

  private isRiskRelevant(record: ImmexRecord): boolean {
    if (!record.activo) return false;
    if ((record.status ?? '').toLowerCase() === 'cerrado') return false;
    if ((record.movimiento ?? '').toLowerCase() === 'exportación' || (record.movimiento ?? '').toLowerCase() === 'exportacion') return false;
    return true;
  }

  save(): void {
    const value = Number(this.form.valorUnitario);
    if (!Number.isFinite(value) || value <= 0) {
      this.error = 'Movimientos: el valor unitario debe ser mayor a 0.';
      return;
    }
    const payload = this.normalizeForm();
    const request$ = this.editingId
      ? this.service.update(this.editingId, payload)
      : this.service.create(payload);

    request$.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: (err: unknown) => {
        this.error = 'Movimientos: ' + this.extractHttpMessage(err, 'No se pudo guardar el registro.');
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
      razonSocial: record.razonSocial ?? this.service.getCompanyFilter(),
      cantidad: record.cantidad,
      valorUnitario: record.valorUnitario,
      fechaIngreso: record.fechaIngreso,
      fechaVencimiento: record.fechaVencimiento,
      status: record.status,
      activo: record.activo,
      notas: record.notas ?? '',
      auditor: record.auditor ?? '',
    };
    this.activeView = 'movement';
    void this.router.navigateByUrl('/dashboard/movement', { state: { editingRecord: record } });
  }

  remove(record: ImmexRecord): void {
    if (!record.id) return;
    this.service.remove(record.id).subscribe({
      next: () => this.load(),
      error: (err: unknown) => {
        this.error = this.extractHttpMessage(err, 'No se pudo eliminar el registro.');
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
    const date = new Date(value + 'T00:00:00');
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }

  explainException(record: ImmexRecord): void {
    const key = this.exceptionKey(record);
    this.exceptionExplanations[key] = this.buildExceptionExplanation(record);
  }

  getExceptionExplanation(record: ImmexRecord): string {
    return this.exceptionExplanations[this.exceptionKey(record)] || '';
  }

  private buildExceptionExplanation(record: ImmexRecord): string {
    const pedimento = record.pedimento || 'este pedimento';
    if (record.status === 'Conciliación') {
      return `El pedimento ${pedimento} está en conciliación porque la información no cuadra entre Data Stage y el agente aduanal. Revisa primero cantidades, fechas y valor declarado; luego corrige el documento que tenga el dato equivocado y vuelve a conciliar.`;
    }
    const days = this.daysLeft(record);
    if (days <= 0) {
      return `El pedimento ${pedimento} ya venció. Revisa si todavía tiene saldo, decide si se exporta, se regulariza o se retorna, y deja evidencia de la corrección.`;
    }
    if (days <= 30) {
      return `El pedimento ${pedimento} está muy cerca de vencer. Verifica el saldo y confirma con el agente aduanal si la cantidad importada y la descargada son las mismas para evitar diferencias.`;
    }
    return `El pedimento ${pedimento} requiere revisión porque hay una diferencia de control documental. Valida las cantidades, el material y el pedimento en ambos lados antes de cerrarlo.`;
  }

  private exceptionKey(record: ImmexRecord): string {
    return record.id || `${record.pedimento}|${record.material}|${record.fechaIngreso}`;
  }

  private syncView(): void {
    const path = this.router.url.split('?')[0].split('#')[0].replace(/^\//, '') || 'dashboard';
    const routeMap: Record<string, DashboardView> = {
      dashboard: 'dashboard',
      'dashboard/movement': 'movement',
      'dashboard/ledger': 'ledger',
      'dashboard/flow': 'flow',
      'dashboard/summary': 'summary',
      'dashboard/risk': 'risk',
      'dashboard/peps': 'peps',
      'dashboard/production': 'production',
      'dashboard/close': 'close',
      'dashboard/audit': 'audit',
      inventory: 'dashboard',
    };
    this.activeView = routeMap[path] ?? 'dashboard';
    document.querySelector('.page-host')?.scrollTo(0, 0);
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
    };
  }

  private toRecord(row: LoteMaterialView): ImmexRecord {
    return {
      id: row.id,
      pedimento: row.pedimento,
      material: row.material,
      proveedor: row.razonSocial || '—',
      movimiento: 'Entrada',
      razonSocial: row.razonSocial,
      cantidad: Number(row.saldo || 0),
      valorUnitario: Number(row.valorUnitarioMxn || 0),
      fechaIngreso: row.fechaEntrada ?? row.fechaVencimiento ?? new Date().toISOString().slice(0, 10),
      fechaVencimiento: row.fechaVencimiento ?? row.fechaEntrada ?? new Date().toISOString().slice(0, 10),
      status: row.estado,
      activo: row.saldo > 0,
      notas: row.tipoMaterial,
      auditor: 'Sistema',
    };
  }

  private extractHttpMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 400) {
        return error.error?.message || error.error?.error || error.message || fallback;
      }
      const detail = typeof error.error === 'string'
        ? error.error
        : error.error?.message || error.message;
      return detail ? `${fallback} ${detail}` : fallback;
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
