import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CollectionSnapshot, DashboardInventorySummary, ImmexCloseReport, ImmexDashboardSummary, ImmexRecord, PepsOperationResult, ProduccionMensualRequest, ProduccionMensualResult, AuditEvent, Anexo24Query, RazonSocialOption, LoteMaterialView } from './models';
import { CompanyFilterService } from './company-filter.service';

@Injectable({ providedIn: 'root' })
export class ImmexService {
  private readonly http = inject(HttpClient);
  private readonly companyFilter = inject(CompanyFilterService);
  private readonly apiUrl = '/api/records';
  private readonly dashboardUrl = '/api/records/summary';
  private readonly inventoryDashboardUrl = '/api/dashboard/inventory';
  private readonly closeReportUrl = '/api/records/close-report';
  private readonly pepsConsumeUrl = '/api/records/peps/consume';
  private readonly auditUrl = '/api/audit';
  private readonly lotesUrl = '/api/lotes';
  private readonly collectionsUrl = '/api/admin/collections';
  private readonly importPedimentoUrl = '/api/pedimentos/import';
  private readonly produccionMensualUrl = '/api/produccion/mensual';
  private readonly anexo24Url = '/api/reportes/anexo24';
  private readonly razonesSocialesUrl = '/api/razones-sociales';

  list(): Observable<ImmexRecord[]> {
    return this.http.get<ImmexRecord[]>(this.withCompany(this.apiUrl));
  }

  summary(): Observable<ImmexDashboardSummary> {
    return this.http.get<ImmexDashboardSummary>(this.withCompany(this.dashboardUrl));
  }

  inventorySummary(): Observable<DashboardInventorySummary> {
    return this.http.get<DashboardInventorySummary>(this.withCompany(this.inventoryDashboardUrl));
  }

  closeReport(): Observable<ImmexCloseReport> {
    return this.http.get<ImmexCloseReport>(this.withCompany(this.closeReportUrl));
  }

  simulatePeps(payload: {
    material: string;
    quantity: number;
    reason?: string;
    auditor?: string;
  }): Observable<PepsOperationResult> {
    return this.http.post<PepsOperationResult>(`${this.pepsConsumeUrl}/simulate`, payload);
  }

  applyPeps(payload: {
    material: string;
    quantity: number;
    reason?: string;
    auditor?: string;
  }): Observable<PepsOperationResult> {
    return this.http.post<PepsOperationResult>(`${this.pepsConsumeUrl}/apply`, payload);
  }

  revertPeps(payload: {
    material: string;
    quantity: number;
    reason?: string;
    auditor?: string;
  }): Observable<PepsOperationResult> {
    return this.http.post<PepsOperationResult>(`${this.pepsConsumeUrl}/revert`, payload);
  }

  auditEvents(): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(this.auditUrl);
  }

  lotes(material?: string, razonSocial?: string): Observable<LoteMaterialView[]> {
    const params = new URLSearchParams();
    if (material) params.set('material', material);
    if (razonSocial && razonSocial !== 'all') params.set('razonSocial', razonSocial);
    const query = params.toString();
    return this.http.get<LoteMaterialView[]>(this.lotesUrl + (query ? `?${query}` : ''));
  }

  collections(): Observable<CollectionSnapshot[]> {
    return this.http.get<CollectionSnapshot[]>(this.collectionsUrl);
  }

  razonesSociales(): Observable<RazonSocialOption[]> {
    return this.http.get<RazonSocialOption[]>(this.razonesSocialesUrl);
  }

  importPedimento(payload: {
    numeroPedimento: string;
    fecha: string;
    razonSocial: string;
    partidas: Array<{ material: string; cantidad: number; valorUnitarioMxn: number }>;
  }): Observable<any> {
    return this.http.post<any>(this.importPedimentoUrl, payload);
  }

  productionPreview(payload: ProduccionMensualRequest): Observable<ProduccionMensualResult> {
    return this.http.post<ProduccionMensualResult>(`${this.produccionMensualUrl}/preview`, payload);
  }

  productionExecute(payload: ProduccionMensualRequest): Observable<ProduccionMensualResult> {
    return this.http.post<ProduccionMensualResult>(this.produccionMensualUrl, payload);
  }

  downloadAnexo24(query: Anexo24Query): Observable<Blob> {
    const params = new URLSearchParams();
    const global = this.companyFilter.selectedRazonSocial;
    const razonSocial = query.razonSocial ?? (global === 'all' ? undefined : global);
    if (razonSocial) params.set('razonSocial', razonSocial);
    if (query.mes) params.set('mes', query.mes);
    return this.http.get(this.anexo24Url + (params.toString() ? `?${params.toString()}` : ''), { responseType: 'blob' });
  }

  create(record: ImmexRecord): Observable<ImmexRecord> {
    return this.http.post<ImmexRecord>(this.apiUrl, record);
  }

  update(id: string, record: ImmexRecord): Observable<ImmexRecord> {
    return this.http.put<ImmexRecord>(`${this.apiUrl}/${id}`, record);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  setCompanyFilter(value: string): void {
    this.companyFilter.setSelectedRazonSocial(value);
  }

  getCompanyFilter(): string {
    return this.companyFilter.selectedRazonSocial;
  }

  private withCompany(url: string): string {
    const company = this.companyFilter.selectedRazonSocial;
    if (!company || company === 'all') return url;
    const separator = url.includes('?') ? '&' : '?';
    return `${url}${separator}razonSocial=${encodeURIComponent(company)}`;
  }
}
