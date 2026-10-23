import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faArrowRight, faCheck, faFileInvoice, faFileLines, faRotate, faUpload, faWandMagicSparkles } from '@fortawesome/free-solid-svg-icons';

type ImportPartida = {
  partida: string;
  material: string;
  unidad: string;
  regimenAduanero: string;
  cantidad: number | null;
  valorUnitarioMxn: number | null;
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
          <h1>De esta factura a tu captura IMMEX</h1>
          <p>Revisemos qué datos sí ayudan a llenar Inventarios / Pedimentos y cuáles necesitan confirmación.</p>
        </div>
        <span class="demo-badge"><fa-icon [icon]="faWandMagicSparkles"></fa-icon> Demo interactiva</span>
      </header>

      <section class="demo-intro">
        <div class="intro-symbol"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div>
        <div class="intro-copy"><strong>Sube una factura y revisa los datos sugeridos.</strong><span>La demo usa el PDF de prueba compartido y distingue los datos visibles de los campos aduaneros que requieren confirmación.</span></div>
        <input #invoiceInput class="file-input" type="file" accept=".pdf,application/pdf" (change)="onInvoiceSelected($event)" />
        <button class="upload-button" type="button" (click)="invoiceInput.click()" [disabled]="analyzing"><fa-icon [icon]="faUpload"></fa-icon>Subir documento</button>
        <button class="analyze-button" type="button" (click)="analyzeSample()" [disabled]="analyzing || !fileSelected"><fa-icon [icon]="analyzing ? faRotate : faWandMagicSparkles" [class.spin]="analyzing"></fa-icon>{{ analyzing ? 'Revisando factura…' : analyzed ? 'Analizar de nuevo' : 'Analizar PDF' }}</button>
      </section>

      <section class="exercise-layout">
        <aside class="source-pane">
          <div class="pane-heading"><div><span class="step-label">FACTURA COMERCIAL</span><h2>Documento de importación</h2></div><span class="pdf-chip">PDF</span></div>
          <div class="paper-wrap" [class.empty-paper-wrap]="!fileSelected">
            <div class="empty-document" *ngIf="!fileSelected"><span class="empty-file-icon"><fa-icon [icon]="faUpload"></fa-icon></span><strong>Tu documento aparecerá aquí</strong><span>Sube un PDF para iniciar la revisión simulada.</span></div>
            <article class="paper-preview" *ngIf="fileSelected" [class.scanned]="analyzed">
              <div class="paper-top"><span>FACTURA COMERCIAL</span><span>DOCUMENTO PROPORCIONADO</span></div>
              <div class="paper-caption">FACTURA N°</div>
              <div class="paper-number" [class.marked]="analyzed">2023-1234</div>
              <div class="paper-row"><span>N° DE ORDEN</span><b>PED-12345</b></div>
              <div class="paper-meta"><span>ORIGEN</span><b>MÉXICO · PUERTO DE VERACRUZ</b></div>
              <div class="paper-rule"></div>
              <div class="paper-caption">CONSIGNATARIO</div>
              <div class="paper-row"><b>MADRID, ESPAÑA</b></div>
              <div class="paper-rule"></div>
              <div class="paper-caption">DESCRIPCIÓN DE MERCANCÍA</div>
              <div class="paper-item" [class.marked]="analyzed"><span>Chiles jalapeños frescos<br><small>Para alimentación</small></span><b>2,880</b></div>
              <div class="paper-row"><span>PESO NETO</span><b>630 kg</b></div>
              <div class="paper-row"><span>PESO BRUTO</span><b class="paper-warning">200 kg · revisar</b></div>
              <div class="paper-row value-row"><span>PRECIO UNITARIO</span><b>$20.90 · moneda no indicada</b></div>
              <div class="paper-row"><span>TOTAL FACTURA</span><b>$77,896.77 · moneda no indicada</b></div>
              <div class="paper-seal">DATOS DE<br>PRUEBA</div>
            </article>
            <div class="scan-line" *ngIf="analyzing"></div>
          </div>
          <div class="document-meta" *ngIf="fileSelected"><span class="file-icon"><fa-icon [icon]="faFileLines"></fa-icon></span><div><b>{{ fileName }}</b><small>PDF seleccionado · análisis de demostración</small></div><fa-icon class="file-check" *ngIf="analyzed" [icon]="faCheck"></fa-icon></div>
          <div class="source-note invoice-route-warning" *ngIf="analyzed"><fa-icon [icon]="faFileInvoice"></fa-icon><span>La ruta México → España sugiere una exportación. Confirma que este documento corresponde a una importación IMMEX antes de registrarlo.</span></div>
          <div class="invoice-issues" *ngIf="analyzed"><strong>Revisar antes de usar</strong><span>PED-12345 aparece como número de orden, no como pedimento.</span><span>El peso bruto (200 kg) aparece menor que el neto (630 kg).</span><span>La factura usa “$”, pero no indica la moneda.</span></div>
        </aside>

        <section class="capture-pane">
          <div class="pane-heading form-heading"><div><span class="step-label">DATOS PARA INVENTARIOS / PEDIMENTOS</span><h2>Revisa antes de transferir</h2></div><span class="field-count" [class.ready]="analyzed">{{ analyzed ? '2 datos precargables' : 'Lista para revisar' }}</span></div>
          <div class="waiting-state" *ngIf="!analyzed && !analyzing"><div class="waiting-icon"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><strong>{{ fileSelected ? 'Documento listo para revisar' : 'Primero sube un documento' }}</strong><span>{{ fileSelected ? 'Inicia el análisis simulado para ver los datos comerciales y los campos que requieren confirmación.' : 'Al cargar un PDF aparecerá aquí la vista previa y podrás iniciar el análisis simulado.' }}</span></div>
          <div class="reading-state" *ngIf="analyzing"><span class="reading-orb"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span><strong>Revisando la factura</strong><span>Separando datos comerciales de datos del pedimento…</span><div class="reading-track"><i></i></div></div>
          <div class="suggested-form" *ngIf="analyzed">
            <div class="document-warning"><span class="warning-mark">!</span><span><b>Posible exportación, no importación</b> La factura indica origen México y consignatario en España. Confirma la operación antes de continuar.</span></div>
            <div class="suggestion-banner"><fa-icon [icon]="faCheck"></fa-icon><span><b>2 datos comerciales disponibles</b> Los campos faltantes quedan vacíos.</span></div>
            <div class="field-grid">
              <label class="demo-field"><span>Número de pedimento <small>No aparece</small></span><input [(ngModel)]="draft.numeroPedimento" name="demoNumero" placeholder="Captura el pedimento" /></label>
              <label class="demo-field"><span>Fecha de entrada <small>No aparece</small></span><input [(ngModel)]="draft.fecha" name="demoFecha" type="date" /></label>
              <label class="demo-field"><span>Razón social IMMEX <small>Selecciona tu empresa</small></span><select [(ngModel)]="draft.razonSocial" name="demoRazon" (ngModelChange)="setCompany($event)"><option value="">Selecciona una empresa</option><option *ngFor="let company of companyOptions" [value]="company.nombre">{{ company.nombre }}</option></select></label>
              <label class="demo-field"><span>RFC <small>Se completa al elegir</small></span><input [(ngModel)]="draft.rfc" name="demoRfc" placeholder="RFC de la empresa IMMEX" /></label>
            </div>
            <div class="partida-divider"><span>LÍNEA DE FACTURA · 01</span><i></i><b>Consecutivo sugerido · régimen por confirmar</b></div>
            <div class="field-grid partida-fields">
              <label class="demo-field full-field"><span>Material <small class="confirmed">Descripción de factura</small></span><input [(ngModel)]="draft.partidas[0].material" name="demoMaterial" /></label>
              <label class="demo-field"><span>Unidad <small>Por confirmar</small></span><input [(ngModel)]="draft.partidas[0].unidad" name="demoUnidad" placeholder="La factura no la aclara" /></label>
              <label class="demo-field"><span>Régimen aduanero <small>No aparece</small></span><input [(ngModel)]="draft.partidas[0].regimenAduanero" name="demoRegimen" placeholder="Confirma el régimen" /></label>
              <label class="demo-field"><span>Cantidad facturada <small>Unidad por validar</small></span><input [(ngModel)]="draft.partidas[0].cantidad" name="demoCantidad" type="number" min="1" /></label>
              <label class="demo-field"><span>Valor unitario · MXN <small>Moneda incierta</small></span><input [(ngModel)]="draft.partidas[0].valorUnitarioMxn" name="demoValor" type="number" step="0.01" min="0.01" placeholder="No se precargó" /></label>
            </div>
            <div class="invoice-value-note"><b>En la factura:</b> unitario $20.90 · subtotal $60,192.00 · cargos $17,704.77 · total $77,896.77. La moneda no está especificada, por eso no se copió al campo MXN.</div>
          </div>
          <footer class="capture-footer"><div class="review-reminder"><fa-icon [icon]="faCheck"></fa-icon><span>Solo se transfiere lo encontrado. Nada se guarda automáticamente.</span></div><button type="button" class="continue-button" [disabled]="!analyzed" (click)="continueToInventory()">Llevar datos disponibles al registro <fa-icon [icon]="faArrowRight"></fa-icon></button></footer>
        </section>
      </section>

      <footer class="demo-disclaimer"><fa-icon [icon]="faWandMagicSparkles"></fa-icon><span>Simulación preparada con la factura de prueba. El PDF que elijas activa la experiencia, pero no se procesa con OCR/IA; confirma los datos y completa el pedimento oficial antes de guardar.</span></footer>
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
  protected readonly faUpload = faUpload;
  protected analyzing = false;
  protected analyzed = false;
  protected fileSelected = false;
  protected fileName = '';
  protected draft = this.exampleDraft();
  protected readonly companyOptions = [
    { nombre: 'Aerosistemas del Norte, S.A. de C.V.', rfc: 'ASN9403159K2' },
    { nombre: 'Componentes Industriales del Bajío, S.A. de C.V.', rfc: 'CIB0209187Q4' },
  ];

  protected setCompany(razonSocial: string): void {
    this.draft.rfc = this.companyOptions.find((company) => company.nombre === razonSocial)?.rfc ?? '';
  }

  protected onInvoiceSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      this.fileSelected = false;
      this.fileName = '';
      this.analyzed = false;
      return;
    }
    this.fileSelected = true;
    this.fileName = file.name;
    this.analyzed = false;
  }

  protected analyzeSample(): void {
    if (this.analyzing || !this.fileSelected) return;
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
      numeroPedimento: '',
      fecha: '',
      razonSocial: '',
      rfc: '',
      partidas: [{
        partida: '01',
        material: 'Chiles jalapeños frescos',
        unidad: '',
        regimenAduanero: '',
        cantidad: 2880,
        valorUnitarioMxn: null,
      }],
    };
  }
}
