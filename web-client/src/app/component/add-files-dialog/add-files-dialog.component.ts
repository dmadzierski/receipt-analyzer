import {Component, Inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {FileUploadComponent} from '../file-upload/file-upload.component';
import {FileGroupService} from '../../service/file-group.service';
import {FileType} from '../../model/receipt.model';

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
    MatSelectModule,
    FileUploadComponent,
  ],
})
export class AddFilesDialogComponent {
  fileTypes = Object.values(FileType);
  fileType: FileType = FileType.PDF;
  readonly isOriginal = true;
  files: File[] = [];
  submitting = false;


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
    this.fileGroupService.uploadFiles(this.data.receiptId, this.files, this.fileType, this.isOriginal).subscribe({
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
