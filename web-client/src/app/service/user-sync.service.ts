import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable, shareReplay, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class UserSyncService {
  private readonly http = inject(HttpClient);
  private sync$?: Observable<void>;

  ensureSynced(): Observable<void> {
    this.sync$ ??= this.http.post<void>('/api/users/sync/me', {}).pipe(
      tap({error: () => (this.sync$ = undefined)}),
      shareReplay(1),
    );
    return this.sync$;
  }
}
