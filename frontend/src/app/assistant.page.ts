import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faArrowRight, faBoxesStacked, faCheck, faCircleExclamation, faFileCircleCheck, faFileInvoice, faRotate, faShieldHalved, faWandMagicSparkles } from '@fortawesome/free-solid-svg-icons';
import { ComplianceAssistantResponse } from './models';
import { ImmexService } from './immex.service';

type DemoFinding = { level: 'critical' | 'review' | 'ready'; title: string; detail: string; evidence: string; action: string };

@Component({
  selector: 'app-assistant-page',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
  template: `
    <main class="copilot-page">
      <section class="copilot-hero">
        <div class="hero-orbit orbit-one"></div><div class="hero-orbit orbit-two"></div>
        <div class="hero-content">
          <div class="copilot-brand"><span class="brand-mark"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span><span>IMMEX <b>INTELLIGENCE</b></span><span class="prototype-chip">DEMO INTERACTIVA</span></div>
          <p class="hero-kicker">TU OPERACIÓN, CON MÁS CLARIDAD</p>
          <h1>Un copiloto para anticiparte.<br><span>No para reemplazarte.</span></h1>
          <p class="hero-description">Imagina revisar tus pedimentos, inventario y alertas en un solo lugar. La IA encuentra señales, explica qué encontró y deja la decisión final en manos de tu equipo.</p>
          <div class="hero-actions"><button type="button" class="hero-button" (click)="runDemo()"><fa-icon [icon]="faSparkles"></fa-icon>{{ demoComplete ? 'Volver a ejecutar demo' : 'Ver análisis de ejemplo' }}<fa-icon [icon]="faArrowRight"></fa-icon></button><span class="hero-note"><fa-icon [icon]="faShieldHalved"></fa-icon> Revisión humana antes de cada cambio</span></div>
        </div>
        <div class="hero-visual" aria-hidden="true"><div class="visual-ring ring-outer"></div><div class="visual-ring ring-inner"></div><div class="visual-core"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><div class="orbit-node node-a"><fa-icon [icon]="faFileInvoice"></fa-icon><span>Pedimentos</span></div><div class="orbit-node node-b"><fa-icon [icon]="faBoxesStacked"></fa-icon><span>Inventario</span></div><div class="orbit-node node-c"><fa-icon [icon]="faShieldHalved"></fa-icon><span>Cumplimiento</span></div><div class="orbit-node node-d"><fa-icon [icon]="faFileCircleCheck"></fa-icon><span>Cierres</span></div></div>
        <div class="hero-foot"><span><i></i> ASISTENCIA CONTEXTUAL</span><span>DATOS DE TU OPERACIÓN · DECISIONES DE TU EQUIPO</span></div>
      </section>

      <section class="workflow-section">
        <div class="section-heading"><div><p class="eyebrow">ASÍ SE VERÍA EN TU DÍA A DÍA</p><h2>De los datos a una decisión clara</h2><p>Una experiencia diseñada para acompañar el flujo de importaciones sin añadir pasos innecesarios.</p></div><span class="workflow-caption">01 — 03</span></div>
        <div class="workflow-grid"><article class="workflow-card"><span class="workflow-number">01</span><div class="workflow-icon upload-icon"><fa-icon [icon]="faFileInvoice"></fa-icon></div><h3>Reúne el contexto</h3><p>Pedimentos, partidas y saldos existentes se consultan desde la operación IMMEX.</p><div class="mini-document"><span class="doc-icon"><fa-icon [icon]="faFileInvoice"></fa-icon></span><span><b>Pedimento de importación</b><small>Datos operativos disponibles</small></span><span class="doc-status">LISTO</span></div></article><article class="workflow-card"><span class="workflow-number">02</span><div class="workflow-icon analysis-icon"><fa-icon [icon]="faSparkles"></fa-icon></div><h3>Encuentra señales</h3><p>La revisión cruza fechas, cantidades, campos requeridos y diferencias que merecen atención.</p><div class="signal-list"><span><i class="signal-dot red"></i> 1 incidencia prioritaria</span><span><i class="signal-dot amber"></i> 2 datos por confirmar</span></div></article><article class="workflow-card"><span class="workflow-number">03</span><div class="workflow-icon decision-icon"><fa-icon [icon]="faCheck"></fa-icon></div><h3>Tu equipo decide</h3><p>La plataforma explica cada hallazgo y propone el siguiente paso, sin realizar cambios por su cuenta.</p><div class="decision-line"><span>Recomendación</span><b>Revisar antes de guardar</b><fa-icon [icon]="faArrowRight"></fa-icon></div></article></div>
      </section>

      <section class="analysis-section" id="demo-analysis">
        <div class="analysis-head"><div><span class="analysis-tag"><fa-icon [icon]="faSparkles"></fa-icon> VISTA PREVIA DEL COPILOTO</span><h2>Así podría verse una revisión</h2><p>Ejemplo ilustrativo con datos de demostración. Explora cómo se presentarían los hallazgos.</p></div><button type="button" class="refresh-button" (click)="runDemo()" aria-label="Ejecutar demostración"><fa-icon [icon]="faRotate"></fa-icon><span>Ejecutar demo</span></button></div>
        <div class="analysis-layout">
          <aside class="analysis-sidebar"><span class="sidebar-label">CENTRO DE ANÁLISIS</span><div class="analysis-profile"><div class="profile-avatar"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><div><b>Copiloto IMMEX</b><small>{{ demoComplete ? 'Análisis listo' : 'Esperando análisis' }}</small></div><span class="online-dot"></span></div><div class="scope-box"><span>EMPRESA ANALIZADA</span><b>{{ analysis?.scope || 'Razón social seleccionada' }}</b><small>Contexto de la operación actual</small></div><div class="source-list"><span class="source-title">CAPAS DE ANÁLISIS · EJEMPLO</span><div><fa-icon [icon]="faFileInvoice"></fa-icon> Pedimentos y partidas <b>Contexto</b></div><div><fa-icon [icon]="faBoxesStacked"></fa-icon> Saldos de inventario <b>Contexto</b></div><div><fa-icon [icon]="faShieldHalved"></fa-icon> Reglas de cumplimiento <b>Validación</b></div></div><div class="privacy-note"><fa-icon [icon]="faShieldHalved"></fa-icon><span><b>Control en tus manos</b><small>La revisión es consultiva. No modifica ni guarda registros.</small></span></div></aside>
          <div class="analysis-main"><div class="analysis-summary" [class.running]="running"><div class="summary-symbol"><fa-icon [icon]="demoComplete ? faSparkles : faCircleExclamation"></fa-icon></div><div class="summary-copy"><span>{{ demoComplete ? 'ANÁLISIS COMPLETADO' : 'LISTO PARA EXPLORAR' }}</span><h3>{{ demoComplete ? (analysis?.summary || 'El copiloto identificó puntos que conviene revisar.') : '¿Qué debería revisar primero?' }}</h3><p>{{ demoComplete ? 'Los hallazgos se basan en la información disponible para la razón social seleccionada.' : 'Ejecuta la demo para ver cómo se convierten los datos en acciones claras y priorizadas.' }}</p></div><div class="summary-score"><strong>{{ demoComplete ? findings.length : '—' }}</strong><span>hallazgos</span></div></div>
            <div class="findings-heading"><div><h3>Hallazgos sugeridos</h3><span>{{ demoComplete ? 'Ordenados por prioridad' : 'Se mostrarán al ejecutar la demo' }}</span></div><span class="human-review"><fa-icon [icon]="faShieldHalved"></fa-icon> Revisión humana</span></div>
            <div class="findings-list"><article class="finding-card" *ngFor="let finding of findings; let i = index" [class]="'finding-card ' + finding.level"><div class="finding-index">0{{ i + 1 }}</div><div class="finding-content"><div class="finding-title"><h4>{{ finding.title }}</h4><span [class]="'severity ' + finding.level">{{ finding.level === 'critical' ? 'PRIORITARIO' : finding.level === 'review' ? 'POR CONFIRMAR' : 'INFORMATIVO' }}</span></div><p>{{ finding.detail }}</p><div class="evidence-line"><span>EVIDENCIA</span>{{ finding.evidence }}</div><div class="suggestion-line"><fa-icon [icon]="faSparkles"></fa-icon><span><b>Siguiente paso sugerido</b>{{ finding.action }}</span></div></div></article>
              <div class="empty-analysis" *ngIf="!findings.length && !running"><div><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><b>Tu análisis aparecerá aquí</b><span>Hallazgos explicados, evidencia y sugerencias para revisar.</span></div>
              <div class="empty-analysis loading-analysis" *ngIf="running"><div class="loader-orbit"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></div><b>Revisando contexto IMMEX…</b><span>Priorizando alertas y conectando cada hallazgo con su evidencia.</span></div>
            </div>
            <div class="analysis-disclaimer"><fa-icon [icon]="faShieldHalved"></fa-icon><span>Las recomendaciones son orientativas y requieren validación del especialista. El copiloto no firma cierres ni modifica información.</span></div>
          </div>
        </div>
      </section>

      <section class="closing-banner"><div class="closing-symbol"><fa-icon [icon]="faSparkles"></fa-icon></div><div><span>INNOVACIÓN CON CRITERIO</span><h2>La IA detecta. Tu equipo decide.</h2><p>Más visibilidad para la operación, con trazabilidad y aprobación humana en cada paso.</p></div><div class="closing-pills"><span><fa-icon [icon]="faShieldHalved"></fa-icon> Control humano</span><span><fa-icon [icon]="faFileCircleCheck"></fa-icon> Evidencia trazable</span></div></section>
    </main>
  `,
  styleUrl: './assistant.page.css',
})
export class AssistantPageComponent implements OnInit {
  private readonly service = inject(ImmexService);
  protected readonly faWandMagicSparkles = faWandMagicSparkles;
  protected readonly faSparkles = faWandMagicSparkles;
  protected readonly faArrowRight = faArrowRight;
  protected readonly faShieldHalved = faShieldHalved;
  protected readonly faFileInvoice = faFileInvoice;
  protected readonly faBoxesStacked = faBoxesStacked;
  protected readonly faFileCircleCheck = faFileCircleCheck;
  protected readonly faCircleExclamation = faCircleExclamation;
  protected readonly faCheck = faCheck;
  protected readonly faRotate = faRotate;
  protected analysis: ComplianceAssistantResponse | null = null;
  protected findings: DemoFinding[] = [];
  protected demoComplete = false;
  protected running = false;

  ngOnInit(): void {
    // La demo espera una acción explícita para que el usuario vea el análisis suceder.
  }

  protected runDemo(): void {
    if (this.running) return;
    this.running = true;
    this.demoComplete = false;
    this.findings = [];
    this.analysis = null;
    const analysisRequest = this.service.complianceAssistant();
    window.setTimeout(() => {
      analysisRequest.subscribe({
        next: (analysis) => {
          this.analysis = analysis;
          this.findings = this.toFindings(analysis);
          this.finishDemo();
        },
        error: () => {
          this.findings = this.sampleFindings();
          this.finishDemo();
        },
      });
    }, 850);
  }

  private finishDemo(): void {
    this.running = false;
    this.demoComplete = true;
  }

  private toFindings(result: ComplianceAssistantResponse): DemoFinding[] {
    const items = result.recommendations.slice(0, 3).map((item): DemoFinding => ({
      level: item.factBased ? 'critical' : 'review',
      title: item.problem,
      detail: item.cause,
      evidence: item.evidence,
      action: item.suggestedAction,
    }));
    return items.length ? items : this.sampleFindings(true);
  }

  private sampleFindings(useDemoLabel = false): DemoFinding[] {
    const label = useDemoLabel ? 'Ejemplo de demostración' : 'Escenario ilustrativo';
    return [
      { level: 'critical', title: 'Fecha límite próxima', detail: 'Un material importado se acerca a la fecha límite de retorno y todavía conserva saldo aduanero.', evidence: `${label} · Pedimento 24 48 3021 9001234 · 18 días restantes`, action: 'Confirma el plan de exportación o retorno con el responsable antes de capturar un movimiento.' },
      { level: 'review', title: 'Cantidad por conciliar', detail: 'La cantidad del archivo del agente y el saldo registrado presentan una diferencia que requiere validación.', evidence: `${label} · Resina ABS natural · Diferencia de 120 kg`, action: 'Compara la partida del pedimento contra el archivo recibido del agente aduanal.' },
      { level: 'ready', title: 'Captura preparada para revisión', detail: 'Se detectaron los campos del pedimento y se puede preparar un borrador para facilitar la captura.', evidence: `${label} · Número, fecha, RFC y 4 partidas`, action: 'Revisa cada campo y confirma manualmente antes de guardar el registro.' },
    ];
  }
}
