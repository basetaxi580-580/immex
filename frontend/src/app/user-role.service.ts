import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type UserRole = 'Especialista' | 'Consulta';

@Injectable({ providedIn: 'root' })
export class UserRoleService {
  private readonly storageKey = 'immex.userRole';
  private readonly selectedRoleSubject = new BehaviorSubject<UserRole>(this.readFromStorage());
  readonly selectedRole$ = this.selectedRoleSubject.asObservable();

  get selectedRole(): UserRole {
    return this.selectedRoleSubject.value;
  }

  setSelectedRole(value: string): void {
    const normalized: UserRole = value === 'Consulta' ? 'Consulta' : 'Especialista';
    this.selectedRoleSubject.next(normalized);
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(this.storageKey, normalized);
    }
  }

  private readFromStorage(): UserRole {
    if (typeof window === 'undefined') return 'Especialista';
    return window.localStorage.getItem(this.storageKey) === 'Consulta' ? 'Consulta' : 'Especialista';
  }
}
