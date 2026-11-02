import {inject} from '@angular/core';
import {CanActivateFn} from '@angular/router';
import {catchError, map, of} from 'rxjs';
import {UserSyncService} from './user-sync.service';

export const userSyncGuard: CanActivateFn = () =>
  inject(UserSyncService).ensureSynced().pipe(
    map(() => true),
    catchError(() => of(false)),
  );
