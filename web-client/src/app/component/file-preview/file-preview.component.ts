import {Component, inject, Input} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {FileType} from '../../model/receipt.model';
import {environment} from '../../../environments/environment';

const MIME_TYPES: Partial<Record<FileType, string>> = {
  [FileType.JPEG]: 'image/jpeg',
  [FileType.PNG]: 'image/png',
  [FileType.WEBP]: 'image/webp',
};

@Component({
  selector: 'app-file-preview',
  templateUrl: './file-preview.component.html',
  styleUrl: './file-preview.component.scss',
})
export class FilePreviewComponent {
  private readonly http = inject(HttpClient);
  private readonly sanitizer = inject(DomSanitizer);

  protected readonly FileType = FileType;
  @Input() type: FileType = FileType.JPEG;

  imageUrls: SafeUrl[] = [];
  jsonContents: string[] = [];
  loading = false;
  error = false;

  private objectUrls: string[] = [];
  private requestId = 0;

  @Input()
  set fileIds(ids: string[]) {
    this.load(ids ?? []);
  }

  private load(ids: string[]): void {
    const current = ++this.requestId;
    this.release();
    this.error = false;
    this.loading = ids.length > 0;

    ids.forEach((id, index) => {
      this.http.get(`${environment.apiUrl}/receipt-files/${id}`, {responseType: 'blob'}).subscribe({
        next: async (blob) => {
          if (current !== this.requestId) return;
          if (this.type === FileType.JSON) {
            this.jsonContents[index] = this.format(await blob.text());
          } else {
            const url = URL.createObjectURL(new Blob([blob], {type: MIME_TYPES[this.type] ?? blob.type}));
            this.objectUrls.push(url);
            this.imageUrls[index] = this.sanitizer.bypassSecurityTrustUrl(url);
          }
          this.loading = false;
        },
        error: () => {
          if (current === this.requestId) {
            this.error = true;
            this.loading = false;
          }
        }
      });
    });
  }

  private format(text: string): string {
    try {
      return JSON.stringify(JSON.parse(text), (_, v) => this.shorten(v), 2);
    } catch {
      return text;
    }
  }

  private shorten(value: unknown): unknown {
    const max = 200;
    return typeof value === 'string' && value.length > max
      ? `${value.slice(0, max)}… (${value.length - max} more characters)`
      : value;
  }

  private release(): void {
    this.objectUrls.forEach(url => URL.revokeObjectURL(url));
    this.objectUrls = [];
    this.imageUrls = [];
    this.jsonContents = [];
  }
}
