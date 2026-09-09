import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({providedIn: 'root'})
export class UserSyncService {
  private readonly http = inject(HttpClient);
  private readonly keycloak = inject(Keycloak);
  private synced = false;

  syncMeOnce(): void {
    if (this.synced || !this.keycloak.authenticated) {
      return;
    }

    this.synced = true;

    this.http.post('/api/users/sync/me', {}).subscribe({
      next: () => console.info('User synced'),
      error: (error) => {
        console.error('User sync failed', error);
        this.synced = false;
      },
    });
  }
}
