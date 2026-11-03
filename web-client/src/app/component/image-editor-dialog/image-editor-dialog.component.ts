import {Component, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {CropperPosition, Dimensions, ImageCroppedEvent, ImageCropperComponent, LoadedImage, OutputFormat} from 'ngx-image-cropper';
import {firstValueFrom} from 'rxjs';
import {FileType} from '../../model/receipt.model';
import {FileGroupService} from '../../service/file-group.service';
import {environment} from '../../../environments/environment';

export interface ImageEditorDialogData {
  receiptId: string;
  fileIds: string[];
  type: FileType;
}

interface EditablePage {
  original: File;
  thumbnail: string;
  rotation: number;
  cropper?: CropperPosition;
  imageSize?: Dimensions;
  result?: Blob;
  edited: boolean;
}

const IMAGE_FORMATS: Partial<Record<FileType, { format: OutputFormat; mime: string; extension: string }>> = {
  [FileType.JPEG]: {format: 'jpeg', mime: 'image/jpeg', extension: 'jpg'},
  [FileType.PNG]: {format: 'png', mime: 'image/png', extension: 'png'},
  [FileType.WEBP]: {format: 'webp', mime: 'image/webp', extension: 'webp'},
};

@Component({
  selector: 'app-image-editor-dialog',
  standalone: true,
  templateUrl: './image-editor-dialog.component.html',
  styleUrl: './image-editor-dialog.component.scss',
  imports: [MatDialogModule, MatButtonModule, MatIconModule, MatTooltip, ImageCropperComponent],
})
export class ImageEditorDialogComponent implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly fileGroupService = inject(FileGroupService);
  private readonly dialogRef = inject(MatDialogRef<ImageEditorDialogComponent>);
  readonly data = inject<ImageEditorDialogData>(MAT_DIALOG_DATA);
  readonly output = IMAGE_FORMATS[this.data.type];

  @ViewChild(ImageCropperComponent) private cropperComponent?: ImageCropperComponent;

  pages: EditablePage[] = [];
  current = 0;
  loading = true;
  error = false;
  saving = false;
  private cropTarget?: EditablePage;

  get page(): EditablePage | undefined {
    return this.pages[this.current];
  }

  get anyEdited(): boolean {
    return this.pages.some(page => page.edited);
  }

  async ngOnInit(): Promise<void> {
    try {
      const blobs = await Promise.all(this.data.fileIds.map(id =>
        firstValueFrom(this.http.get(`${environment.apiUrl}/receipt-files/${id}`, {responseType: 'blob'}))));
      this.pages = blobs.map((blob, index) => ({
        original: new File([blob], `page-${index + 1}`, {type: blob.type || this.output?.mime}),
        thumbnail: URL.createObjectURL(blob),
        rotation: 0,
        edited: false,
      }));
    } catch {
      this.error = true;
    } finally {
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    this.pages.forEach(page => URL.revokeObjectURL(page.thumbnail));
  }

  show(index: number): void {
    this.current = index;
  }

  rotate(clockwise: boolean): void {
    const page = this.page;
    if (!page) return;
    page.rotation = (page.rotation + (clockwise ? 1 : 3)) % 4;
    page.cropper = undefined;
  }

  reset(): void {
    const page = this.page;
    if (!page) return;
    page.rotation = 0;
    page.cropper = undefined;
    page.result = undefined;
    page.edited = false;
    this.cropperComponent?.resetCropperPosition();
  }

  onImageLoaded(image: LoadedImage): void {
    if (this.page) this.page.imageSize = image.transformed.size;
  }

  onCropStart(): void {
    this.cropTarget = this.page;
  }

  onCropped(event: ImageCroppedEvent): void {
    const page = this.cropTarget;
    if (!page || !event.blob) return;
    page.result = event.blob;
    page.edited = page.rotation !== 0 || !this.coversWholeImage(event, page.imageSize);
  }

  async save(): Promise<void> {
    const output = this.output;
    if (!output || this.saving || !this.anyEdited) return;
    this.saving = true;
    try {
      const files = this.pages.map((page, index) => new File(
        [page.edited && page.result ? page.result : page.original],
        `edited-${index + 1}.${output.extension}`,
        {type: output.mime}));
      await firstValueFrom(this.fileGroupService.uploadFiles(this.data.receiptId, files));
      this.dialogRef.close(true);
    } catch {
      this.saving = false;
    }
  }

  cancel(): void {
    this.dialogRef.close(false);
  }

  private coversWholeImage(event: ImageCroppedEvent, size?: Dimensions): boolean {
    if (!size) return true;
    return Math.abs(event.width - size.width) <= 1 && Math.abs(event.height - size.height) <= 1;
  }
}
