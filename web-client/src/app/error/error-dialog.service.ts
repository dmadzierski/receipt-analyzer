import {inject, Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {map} from 'rxjs';
import {ErrorDetails, ErrorDialogComponent, ErrorDialogData} from '../component/error-dialog/error-dialog.component';

const UNKNOWN_ERROR_KEY = 'ERROR.UNKNOWN';

@Injectable({
  providedIn: 'root',
})
export class ErrorDialogService {
  private readonly dialog = inject(MatDialog);
  private readonly translate = inject(TranslateService);

  show(messageKey: string | null, details: Partial<ErrorDetails> = {}): void {
    const key = messageKey ?? UNKNOWN_ERROR_KEY;
    this.translate.get(['ERROR.TITLE', key, UNKNOWN_ERROR_KEY]).pipe(
      map(t => ({
        title: t['ERROR.TITLE'],
        message: t[key] !== key ? t[key] : t[UNKNOWN_ERROR_KEY],
        details: {...details, timestamp: details.timestamp ?? new Date().toISOString()},
      }) as ErrorDialogData)
    ).subscribe(data => {
      if (this.dialog.openDialogs.some(ref => ref.componentInstance instanceof ErrorDialogComponent)) {
        return;
      }
      this.dialog.open(ErrorDialogComponent, {width: '480px', maxWidth: '95vw', data});
    });
  }
}
