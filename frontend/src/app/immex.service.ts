import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ImmexCloseReport, ImmexDashboardSummary, ImmexRecord, PepsOperationResult, CollectionSnapshot } from './models';

@Injectable({ providedIn: 'root' })
export class ImmexService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/records';
  private readonly dashboardUrl = '/api/records/summary';
  private readonly closeReportUrl = '/api/records/close-report';
  private readonly pepsConsumeUrl = '/api/records/peps/consume';
  private readonly auditUrl = '/api/audit';
  private readonly collectionsUrl = '/api/admin/collections';

  list(): Observable<ImmexRecord[]> {
    return this.http.get<ImmexRecord[]>(this.apiUrl);
  }

  summary(): Observable<ImmexDashboardSummary> {
    return this.http.get<ImmexDashboardSummary>(this.dashboardUrl);
  }

  closeReport(): Observable<ImmexCloseReport> {
    return this.http.get<ImmexCloseReport>(this.closeReportUrl);
  }

  consumePeps(payload: {
    material: string;
    quantity: number;
    reason?: string;
    auditor?: string;
  }): Observable<PepsOperationResult> {
    return this.http.post<PepsOperationResult>(this.pepsConsumeUrl, payload);
  }

  auditEvents(): Observable<any[]> {
    return this.http.get<any[]>(this.auditUrl);
  }

  collections(): Observable<CollectionSnapshot[]> {
    return this.http.get<CollectionSnapshot[]>(this.collectionsUrl);
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
}
