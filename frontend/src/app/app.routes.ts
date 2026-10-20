import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { InventoryPageComponent } from './inventory.page';

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
  { path: 'dashboard/audit', component: AppComponent },
  { path: 'inventory', component: InventoryPageComponent },
  { path: '**', redirectTo: 'dashboard' },
];
