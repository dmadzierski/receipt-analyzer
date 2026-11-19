import {HttpInterceptorFn, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {finalize, tap} from 'rxjs';

export const CONSISTENCY_TOKEN_HEADER = 'X-Consistency-Token';

@Injectable({providedIn: 'root'})
export class ConsistencyTokenStore {
  private token: string | null = null;

  current(): string | null {
    return this.token;
  }

  remember(token: string | null): void {
    if (token) this.token = token;
  }

  release(token: string): void {
    if (this.token === token) this.token = null;
  }
}

export const consistencyTokenInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes('/api/')) return next(req);
  const store = inject(ConsistencyTokenStore);
  if (req.method !== 'GET') {
    return next(req).pipe(tap(event => {
      if (event instanceof HttpResponse) store.remember(event.headers.get(CONSISTENCY_TOKEN_HEADER));
    }));
  }
  const token = store.current();
  if (!token) return next(req);
  return next(req.clone({setHeaders: {[CONSISTENCY_TOKEN_HEADER]: token}})).pipe(finalize(() => store.release(token)));
};
