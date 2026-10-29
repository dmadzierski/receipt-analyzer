import {Component, Inject, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

export interface ErrorDetails {
  timestamp: string;
  status?: number;
  statusText?: string;
  code?: string;
  method?: string;
  path?: string;
}

export interface ErrorDialogData {
  title: string;
  message: string;
  details: ErrorDetails;
}

@Component({
  selector: 'app-error-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, DatePipe, TranslatePipe],
  templateUrl: './error-dialog.component.html',
  styleUrl: './error-dialog.component.scss',
})
export class ErrorDialogComponent {
  protected readonly showDetails = signal(false);

  constructor(@Inject(MAT_DIALOG_DATA) protected readonly data: ErrorDialogData) {
  }

  protected toggleDetails(): void {
    this.showDetails.update(value => !value);
  }
}
