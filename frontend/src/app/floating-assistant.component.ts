import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faPaperPlane, faWandMagicSparkles, faXmark } from '@fortawesome/free-solid-svg-icons';

type ChatMessage = { role: 'assistant' | 'user'; text: string };

@Component({
  selector: 'app-floating-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule],
  template: `
    <aside class="assistant-widget" [class.is-open]="isOpen">
      <section class="assistant-panel" *ngIf="isOpen" aria-label="Asistente IMMEX">
        <header class="assistant-header">
          <span class="assistant-avatar"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span>
          <div class="assistant-title"><strong>Asistente IMMEX</strong><span><i></i> Disponible · demo</span></div>
          <button type="button" class="close-assistant" (click)="toggle()" aria-label="Ocultar asistente"><fa-icon [icon]="faXmark"></fa-icon></button>
        </header>
        <div class="assistant-messages" aria-live="polite">
          <article class="message assistant-message"><span class="message-mark"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span><p>¡Hola! 👋 Soy tu asistente IMMEX. Para orientarte mejor, ¿de qué trata la plataforma y qué operaciones quieres gestionar?</p></article>
          <article class="message" *ngFor="let message of messages" [class.user-message]="message.role === 'user'" [class.assistant-message]="message.role === 'assistant'"><span class="message-mark" *ngIf="message.role === 'assistant'"><fa-icon [icon]="faWandMagicSparkles"></fa-icon></span><p>{{ message.text }}</p></article>
          <div class="assistant-suggestions" *ngIf="messages.length === 0"><button type="button" (click)="ask('¿Qué puedo hacer en esta plataforma?')">¿Qué puedo hacer aquí?</button><button type="button" (click)="ask('¿Cómo registro un pedimento?')">¿Cómo registro un pedimento?</button></div>
        </div>
        <form class="assistant-composer" (ngSubmit)="send()"><label class="sr-only" for="floating-assistant-input">Escribe tu pregunta</label><input id="floating-assistant-input" name="floatingAssistantInput" [(ngModel)]="input" placeholder="Escribe tu pregunta…" autocomplete="off" /><button type="submit" [disabled]="!input.trim()" aria-label="Enviar mensaje"><fa-icon [icon]="faPaperPlane"></fa-icon></button></form>
      </section>
      <button class="assistant-launcher" type="button" (click)="toggle()" [attr.aria-expanded]="isOpen" [attr.aria-label]="isOpen ? 'Ocultar asistente' : 'Abrir asistente IMMEX'" [attr.title]="isOpen ? 'Ocultar asistente' : 'Asistente IMMEX'"><fa-icon [icon]="isOpen ? faXmark : faWandMagicSparkles"></fa-icon><span *ngIf="!isOpen">Asistente</span></button>
    </aside>
  `,
  styleUrl: './floating-assistant.component.css',
})
export class FloatingAssistantComponent {
  private readonly cdr = inject(ChangeDetectorRef);
  protected readonly faPaperPlane = faPaperPlane;
  protected readonly faWandMagicSparkles = faWandMagicSparkles;
  protected readonly faXmark = faXmark;
  protected isOpen = false;
  protected input = '';
  protected messages: ChatMessage[] = [];

  protected toggle(): void { this.isOpen = !this.isOpen; }

  protected ask(text: string): void { this.input = text; this.send(); }

  protected send(): void {
    const question = this.input.trim();
    if (!question) return;
    this.messages = [...this.messages, { role: 'user', text: question }];
    this.input = '';
    window.setTimeout(() => {
      this.messages = [...this.messages, { role: 'assistant', text: this.reply(question) }];
      this.cdr.detectChanges();
      document.querySelector('.assistant-messages')?.scrollTo({ top: 9999, behavior: 'smooth' });
    }, 450);
  }

  private reply(question: string): string {
    const normalized = question.toLocaleLowerCase('es');
    if (normalized.includes('pedimento')) return 'Desde Importaciones puedes capturar pedimentos y sus partidas. Revisa número, fecha, régimen y cantidades antes de guardar. La demo de captura puede sugerir datos comerciales a partir de una factura.';
    if (normalized.includes('factura') || normalized.includes('documento') || normalized.includes('pdf')) return 'La demo de captura permite seleccionar un PDF y simula cómo se sugerirían algunos datos para inventarios. No procesa archivos con OCR/IA real; valida siempre la información aduanera.';
    if (normalized.includes('inventario') || normalized.includes('plataforma') || normalized.includes('hacer') || normalized.includes('trata')) return 'IMMEX ayuda a controlar inventarios y operaciones de importación, producción, exportación y descargos, con seguimiento de pedimentos, saldos y vencimientos. ¿Qué proceso quieres revisar primero?';
    return 'Puedo orientarte sobre operaciones IMMEX, inventarios, pedimentos, movimientos y vencimientos. ¿Qué parte de la operación quieres conocer?';
  }
}
