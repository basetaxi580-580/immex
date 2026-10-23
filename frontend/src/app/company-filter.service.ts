import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CompanyFilterService {
  private readonly storageKey = 'immex.selectedRazonSocial';
  private readonly selectedRazonSocialSubject = new BehaviorSubject<string>(this.readFromStorage());
  readonly selectedRazonSocial$ = this.selectedRazonSocialSubject.asObservable();

  get selectedRazonSocial(): string {
    return this.selectedRazonSocialSubject.value;
  }

  setSelectedRazonSocial(value: string): void {
    const normalized = value || 'all';
    this.selectedRazonSocialSubject.next(normalized);
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(this.storageKey, normalized);
    }
  }

  private readFromStorage(): string {
    if (typeof window === 'undefined') {
      return 'all';
    }
    return window.localStorage.getItem(this.storageKey) || 'all';
  }
}
