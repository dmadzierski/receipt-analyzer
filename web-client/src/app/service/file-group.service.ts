import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FileGroupService {
  private readonly httpClient = inject(HttpClient);

  uploadFiles(receiptId: string, files: File[]): Observable<void> {
    const body = new FormData();

    for (const file of files) {
      body.append('file', file, file.name);
    }

    return this.httpClient.post<void>(`/api/receipt-group-files/${receiptId}`, body);
  }
}