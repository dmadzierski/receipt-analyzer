import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, from, map, Observable, of, throwError} from 'rxjs';
import {ErrorDialogService} from './error-dialog.service';
import {ErrorDetails} from '../component/error-dialog/error-dialog.component';

interface ErrorResponse {
  code: string;
  status?: number;
  error?: string;
  timestamp?: string;
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorDialog = inject(ErrorDialogService);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const requestDetails: Partial<ErrorDetails> = {method: req.method, path: new URL(req.urlWithParams, document.baseURI).pathname};
        if (error.status === 0) {
          errorDialog.show('ERROR.CONNECTION', requestDetails);
        } else {
          readErrorBody(error).subscribe(body => errorDialog.show(body?.code ?? null, {
            ...requestDetails,
            status: error.status,
            statusText: body?.error ?? error.statusText,
            code: body?.code,
            timestamp: body?.timestamp,
          }));
        }
      }
      return throwError(() => error);
    })
  );
};

function readErrorBody(error: HttpErrorResponse): Observable<ErrorResponse | null> {
  if (error.error instanceof Blob) {
    return from(error.error.text()).pipe(
      map(text => asErrorResponse(JSON.parse(text))),
      catchError(() => of(null))
    );
  }
  return of(asErrorResponse(error.error));
}

function asErrorResponse(body: unknown): ErrorResponse | null {
  return typeof (body as ErrorResponse)?.code === 'string' ? body as ErrorResponse : null;
}
