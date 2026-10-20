import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { InventoryPageComponent } from './inventory.page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: AppComponent },
  { path: 'inventory', component: InventoryPageComponent },
  { path: '**', redirectTo: 'dashboard' },
];
