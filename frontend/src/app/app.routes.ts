import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { InventoryPageComponent } from './inventory.page';
import { AuditorPageComponent } from './auditor.page';
import { AssistantPageComponent } from './assistant.page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: AppComponent },
  { path: 'dashboard/movement', component: AppComponent },
  { path: 'dashboard/ledger', component: AppComponent },
  { path: 'dashboard/flow', component: AppComponent },
  { path: 'dashboard/summary', component: AppComponent },
  { path: 'dashboard/risk', component: AppComponent },
  { path: 'dashboard/peps', component: AppComponent },
  { path: 'dashboard/close', component: AppComponent },
  { path: 'dashboard/audit', component: AuditorPageComponent },
  { path: 'dashboard/production', component: AppComponent },
  { path: 'dashboard/auditor', redirectTo: 'dashboard/audit' },
  { path: 'assistant', component: AssistantPageComponent },
  { path: 'inventory', component: InventoryPageComponent },
  { path: 'search', component: InventoryPageComponent, data: { searchOnly: true } },
  { path: '**', redirectTo: 'dashboard' },
];
