import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faArrowRight, faCheck, faFileInvoice, faFileLines, faRotate, faWandMagicSparkles } from '@fortawesome/free-solid-svg-icons';

type ImportPartida = {
  partida: string;
  material: string;
  unidad: string;
  regimenAduanero: string;
  cantidad: number;
  valorUnitarioMxn: number;
};

type ImportDraft = {
  numeroPedimento: string;
  fecha: string;
  razonSocial: string;
  rfc: string;
  partidas: ImportPartida[];
};

@Component({
  selector: 'app-assistant-page',
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule, RouterLink],
  template: `
    <main class="capture-demo">
      <header class="demo-heading">
        <div class="heading-copy">
          <a class="back-link" routerLink="/inventory">Inventarios / Pedimentos</a>
          <h1>Del pedimento a tu inventario</h1>
          <p>Prueba cómo el copiloto identifica los datos y los prepara para tu captura.</p>
        </div>
        <span class="demo-badge"><fa-icon [icon]="faWandMagicSparkles"></fa-icon> Demo interactiva</span>
      </header>

      <section class="demo-intro">
        <div class="intro-symbol"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div>
        <div class="intro-copy"><strong>Menos captura manual. Más tiempo para validar.</strong><span>Simularemos la lectura de un pedimento con una partida de importación temporal.</span></div>
        <button class="analyze-button" type="button" (click)="analyzeSample()" [disabled]="analyzing"><fa-icon [icon]="analyzing ? faRotate : faWandMagicSparkles" [class.spin]="analyzing"></fa-icon>{{ analyzing ? 'Leyendo ejemplo…' : analyzed ? 'Volver a simular' : 'Simular lectura' }}</button>
      </section>

      <section class="exercise-layout">
        <aside class="source-pane">
          <div class="pane-heading"><div><span class="step-label">DOCUMENTO DE EJEMPLO</span><h2>Pedimento de importación</h2></div><span class="pdf-chip">PDF</span></div>
          <div class="paper-wrap">
            <article class="paper-preview" [class.scanned]="analyzed">
              <div class="paper-top"><span>ADUANA MÉXICO</span><span>FORMATO DEMO</span></div>
              <div class="paper-caption">PEDIMENTO</div>
              <div class="paper-number" [class.marked]="analyzed">24 48 3021 9001234</div>
              <div class="paper-meta"><span>FECHA DE ENTRADA</span><b>21 OCT 2026</b></div>
              <div class="paper-rule"></div>
              <div class="paper-caption">DATOS DEL IMPORTADOR</div>
              <div class="paper-row"><span>RFC</span><b>ASN9403159K2</b></div>
              <div class="paper-row"><span>RÉGIMEN</span><b>IN — TEMPORAL IMMEX</b></div>
              <div class="paper-rule"></div>
              <div class="paper-caption">PARTIDAS</div>
              <div class="paper-table-head"><span>FRACCIÓN / DESCRIPCIÓN</span><span>CANT.</span></div>
              <div class="paper-item" [class.marked]="analyzed"><span>Resina ABS natural<br><small>Grado inyección</small></span><b>1,200 kg</b></div>
              <div class="paper-row value-row"><span>VALOR UNITARIO</span><b>3.20 MXN</b></div>
              <div class="paper-seal">EJEMPLO<br>ILUSTRATIVO</div>
            </article>
            <div class="scan-line" *ngIf="analyzing"></div>
          </div>
          <div class="document-meta"><span class="file-icon"><fa-icon [icon]="faFileLines"></fa-icon></span><div><b>pedimento_ejemplo.pdf</b><small>1 página · Escenario de práctica</small></div><fa-icon class="file-check" *ngIf="analyzed" [icon]="faCheck"></fa-icon></div>
          <div class="source-note"><fa-icon [icon]="faFileInvoice"></fa-icon><span>Este documento es una muestra para recorrer el ejercicio; no corresponde a una operación real.</span></div>
        </aside>

        <section class="capture-pane">
          <div class="pane-heading form-heading"><div><span class="step-label">CAPTURA SUGERIDA</span><h2>Datos de importación</h2></div><span class="field-count" [class.ready]="analyzed">{{ analyzed ? '7 campos sugeridos' : 'Esperando lectura' }}</span></div>
          <div class="waiting-state" *ngIf="!analyzed && !analyzing"><div class="waiting-icon"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><strong>Tu captura aparecerá aquí</strong><span>Inicia la simulación para ver cómo el copiloto llena los datos del pedimento.</span></div>
          <div class="reading-state" *ngIf="analyzing"><span class="reading-orb"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span><strong>Revisando el documento</strong><span>Identificando importador, fechas y partidas…</span><div class="reading-track"><i></i></div></div>
          <div class="suggested-form" *ngIf="analyzed">
            <div class="suggestion-banner"><fa-icon [icon]="faCheck"></fa-icon><span><b>Datos detectados</b> Revisa las sugerencias antes de usarlas.</span><span class="confidence">Coincidencia alta</span></div>
            <div class="field-grid">
              <label class="demo-field"><span>Número de pedimento <small>Detectado</small></span><input [(ngModel)]="draft.numeroPedimento" name="demoNumero" /></label>
              <label class="demo-field"><span>Fecha de entrada <small>Detectado</small></span><input [(ngModel)]="draft.fecha" name="demoFecha" type="date" /></label>
              <label class="demo-field full-field"><span>Razón social <small>Relacionada por RFC</small></span><input [(ngModel)]="draft.razonSocial" name="demoRazon" /></label>
              <label class="demo-field"><span>RFC <small>Detectado</small></span><input [(ngModel)]="draft.rfc" name="demoRfc" /></label>
            </div>
            <div class="partida-divider"><span>PARTIDA 01</span><i></i><b>Importación temporal</b></div>
            <div class="field-grid partida-fields">
              <label class="demo-field full-field"><span>Material <small>Descripción detectada</small></span><input [(ngModel)]="draft.partidas[0].material" name="demoMaterial" /></label>
              <label class="demo-field"><span>Unidad</span><input [(ngModel)]="draft.partidas[0].unidad" name="demoUnidad" /></label>
              <label class="demo-field"><span>Régimen aduanero</span><input [(ngModel)]="draft.partidas[0].regimenAduanero" name="demoRegimen" /></label>
              <label class="demo-field"><span>Cantidad</span><input [(ngModel)]="draft.partidas[0].cantidad" name="demoCantidad" type="number" min="1" /></label>
              <label class="demo-field"><span>Valor unitario · MXN</span><input [(ngModel)]="draft.partidas[0].valorUnitarioMxn" name="demoValor" type="number" step="0.01" min="0.01" /></label>
            </div>
            <div class="capture-total"><span>Valor estimado de la partida</span><b>{{ lineTotal | currency:'MXN':'symbol':'1.2-2' }}</b></div>
          </div>
          <footer class="capture-footer"><div class="review-reminder"><fa-icon [icon]="faCheck"></fa-icon><span>El equipo revisa y confirma antes de guardar.</span></div><button type="button" class="continue-button" [disabled]="!analyzed" (click)="continueToInventory()">Llevar al formulario IMMEX <fa-icon [icon]="faArrowRight"></fa-icon></button></footer>
        </section>
      </section>

      <footer class="demo-disclaimer"><fa-icon [icon]="faWandMagicSparkles"></fa-icon><span>Demostración de asistencia de captura. Los datos son de ejemplo; la plataforma no los guardará hasta que los revises y confirmes en Inventarios / Pedimentos.</span></footer>
    </main>
  `,
  styleUrl: './assistant.page.css',
})
export class AssistantPageComponent {
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  protected readonly faWandMagicSparkles = faWandMagicSparkles;
  protected readonly faArrowRight = faArrowRight;
  protected readonly faCheck = faCheck;
  protected readonly faFileInvoice = faFileInvoice;
  protected readonly faFileLines = faFileLines;
  protected readonly faRotate = faRotate;
  protected analyzing = false;
  protected analyzed = false;
  protected draft = this.exampleDraft();

  protected get lineTotal(): number {
    const line = this.draft.partidas[0];
    return Number(line?.cantidad || 0) * Number(line?.valorUnitarioMxn || 0);
  }

  protected analyzeSample(): void {
    if (this.analyzing) return;
    this.analyzed = false;
    this.analyzing = true;
    window.setTimeout(() => {
      this.draft = this.exampleDraft();
      this.analyzing = false;
      this.analyzed = true;
      this.cdr.detectChanges();
    }, 1300);
  }

  protected continueToInventory(): void {
    void this.router.navigateByUrl('/inventory', { state: { draftPedimento: this.draft } });
  }

  private exampleDraft(): ImportDraft {
    return {
      numeroPedimento: '24 48 3021 9001234',
      fecha: '2026-10-21',
      razonSocial: 'Aerosistemas del Norte, S.A. de C.V.',
      rfc: 'ASN9403159K2',
      partidas: [{
        partida: '01',
        material: 'Resina ABS natural · grado inyección',
        unidad: 'kg',
        regimenAduanero: 'IMMEX temporal',
        cantidad: 1200,
        valorUnitarioMxn: 3.2,
      }],
    };
  }
}
