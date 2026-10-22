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
  razonSocial?: string;
  rfc?: string;
  partida?: string;
  unidad?: string;
  regimenAduanero?: string;
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
  saldoTotalMxn: number;
  montoEnRiesgoMxn: number;
  pepsCandidate?: ImmexRecord | null;
  warnings: string[];
  pendingItems: ClosePendingItem[];
  estado: string;
}

export interface ClosePendingItem {
  key: string;
  label: string;
  count: number;
  href: string;
}

export interface CloseWorkflowStep {
  key: string;
  label: string;
  responsable: string;
  estado: string;
  pendiente: string;
  detalle: string;
  updatedBy?: string | null;
  updatedAt?: string | null;
}

export interface CloseMonthlyHistoryItem {
  stepLabel: string;
  actor: string;
  changedAt: string;
  field: string;
  oldValue: string;
  newValue: string;
  reason: string;
}

export interface CloseMonthlyStatus {
  periodo: string;
  firmado: boolean;
  firmadoPor?: string | null;
  firmadoAt?: string | null;
  canClose: boolean;
  hasCriticalErrors: boolean;
  progressPct: number;
  pendingCount: number;
  pendingItems: string[];
  steps: CloseWorkflowStep[];
  history: CloseMonthlyHistoryItem[];
  currentResponsible: string;
}

export interface CloseMonthlyStateResponse {
  status: CloseMonthlyStatus;
  canSign: boolean;
  canDownloadAnexo24: boolean;
}

export interface PepsMovement {
  loteId: string;
  pedimento: string;
  material: string;
  cantidad: number;
  saldoAntes: number;
  saldoDespues: number;
  tipoMovimiento: string;
}

export interface PepsOperationResult {
  material: string;
  requestedQuantity: number;
  processedQuantity: number;
  completed: boolean;
  message: string;
  processedAt: string;
  movimientos: PepsMovement[];
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

export interface DashboardMaterialRow {
  material: string;
  pedimento: string;
  rfc: string;
  partida: string;
  unidad: string;
  regimenAduanero: string;
  saldo: number;
  saldoFisico: number;
  saldoAduaneroPendiente: number;
  fechaVencimiento: string | null;
  montoEnRiesgo: number;
  diasRestantes: number;
  estado: string;
}

export interface DashboardInventorySummary {
  saldoTotalMxn: number;
  montoVencidoMxn: number;
  montoPorVencerMxn: number;
  materiales: DashboardMaterialRow[];
}

export interface Anexo24Query {
  razonSocial?: string;
  mes?: string;
}

export interface ProduccionMensualRequest {
  producto: string;
  cantidadProducida: number;
  mermaPct: number;
  auditor?: string;
  observacion?: string;
}

export interface ProduccionMensualLine {
  material: string;
  cantidadBom: number;
  mermaIncluida: number;
  requerido: number;
  descargado: number;
  saldoPendiente: number;
  lotesAfectados: string[];
}

export interface ProduccionMensualResult {
  producto: string;
  cantidadProducida: number;
  mermaPct: number;
  lineas: ProduccionMensualLine[];
  movimientosDescarga: number;
  completado: boolean;
  message: string;
}

export interface RazonSocialOption {
  nombre: string;
  rfc: string;
  programaImmex: string;
  activo: boolean;
}

export interface LoteMaterialView {
  id: string;
  material: string;
  pedimento: string;
  razonSocial: string;
  rfc: string;
  partida: string;
  unidad: string;
  regimenAduanero: string;
  saldo: number;
  saldoFisico: number;
  saldoAduaneroPendiente: number;
  fechaEntrada: string | null;
  fechaVencimiento: string | null;
  diasRestantes: number;
  estado: string;
  valorUnitarioMxn: number;
  tipoMaterial: string;
}

export interface ComplianceRecommendation {
  problem: string;
  cause: string;
  impact: string;
  suggestedAction: string;
  evidence: string;
  factBased: boolean;
}

export interface ComplianceAssistantResponse {
  scope: string;
  summary: string;
  recommendations: ComplianceRecommendation[];
  facts: string[];
  limitations: string[];
}
