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
  razonSocial?: string;
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
  saldo: number;
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
  saldo: number;
  fechaEntrada: string | null;
  fechaVencimiento: string | null;
  diasRestantes: number;
  estado: string;
  valorUnitarioMxn: number;
  tipoMaterial: string;
}
