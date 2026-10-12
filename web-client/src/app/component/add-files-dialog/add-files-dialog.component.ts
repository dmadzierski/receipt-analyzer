import {Component, Inject, OnDestroy} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {FileUploadComponent} from '../file-upload/file-upload.component';
import {FileGroupService} from '../../service/file-group.service';

export interface AddFilesDialogData {
  receiptId: string;
}

@Component({
  selector: 'app-add-files-dialog',
  standalone: true,
  templateUrl: './add-files-dialog.component.html',
  imports: [
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    FileUploadComponent,
  ],
})
export class AddFilesDialogComponent implements OnDestroy {
  files: File[] = [];
  submitting = false;
  private readonly previewUrls = new Map<File, string>();

  get imageFiles(): File[] {
    return this.files.filter(file => file.type.startsWith('image/'));
  }

  previewUrl(file: File): string {
    let url = this.previewUrls.get(file);
    if (!url) {
      url = URL.createObjectURL(file);
      this.previewUrls.set(file, url);
    }
    return url;
  }

  openPreview(file: File): void {
    window.open(this.previewUrl(file), '_blank');
  }

  moveUp(file: File): void {
    this.swapWithNeighbourImage(file, -1);
  }

  moveDown(file: File): void {
    this.swapWithNeighbourImage(file, 1);
  }

  ngOnDestroy(): void {
    this.previewUrls.forEach(url => URL.revokeObjectURL(url));
    this.previewUrls.clear();
  }

  private swapWithNeighbourImage(file: File, direction: -1 | 1): void {
    const images = this.imageFiles;
    const target = images[images.indexOf(file) + direction];
    if (!target) {
      return;
    }
    const a = this.files.indexOf(file);
    const b = this.files.indexOf(target);
    [this.files[a], this.files[b]] = [this.files[b], this.files[a]];
  }

  constructor(
    private readonly dialogRef: MatDialogRef<AddFilesDialogComponent>,
    private readonly fileGroupService: FileGroupService,
    @Inject(MAT_DIALOG_DATA) public data: AddFilesDialogData,
  ) {
  }

  save(): void {
    if (this.files.length === 0 || this.submitting) {
      return;
    }

    this.submitting = true;
    this.fileGroupService.uploadFiles(this.data.receiptId, this.files).subscribe({
      next: () => {
        this.submitting = false;
        this.dialogRef.close(true);
      },
      error: () => {
        this.submitting = false;
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
