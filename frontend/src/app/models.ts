export interface ImmexRecord {
  id?: string;
  pedimento: string;
  material: string;
  proveedor: string;
  movimiento: string;
  cantidad: number;
  valorUnitario: number;
  fechaIngreso: string;
  fechaVencimiento: string;
  status: string;
  activo: boolean;
  notas?: string;
  auditor?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ImmexDashboardSummary {
  totalRecords: number;
  riskTotal: number;
  expiringSoonCount: number;
  alert90Count: number;
  alert60Count: number;
  alert30Count: number;
  entryCount: number;
  consumptionCount: number;
  exportCount: number;
  closeReadyCount: number;
  pepsCandidate?: ImmexRecord | null;
}

export interface ImmexCloseReport {
  generatedAt: string;
  totalRecords: number;
  openRecords: number;
  expiringSoonCount: number;
  riskTotal: number;
  pepsCandidate?: ImmexRecord | null;
  warnings: string[];
}

export interface PepsOperationResult {
  material: string;
  requestedQuantity: number;
  processedQuantity: number;
  completed: boolean;
  message: string;
  processedAt: string;
}

export interface AuditEvent {
  id?: string;
  entityType: string;
  entityId: string;
  action: string;
  actor: string;
  reason?: string;
  details?: string;
  createdAt?: string;
}

export interface CollectionSnapshot {
  name: string;
  count: number;
  sample: string[];
}
