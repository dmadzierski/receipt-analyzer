import {HttpErrorResponse, HttpStatusCode} from '@angular/common/http';

export function isVersionConflict(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict;
}
