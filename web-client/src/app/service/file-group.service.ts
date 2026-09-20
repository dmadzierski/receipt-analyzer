import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FileType} from '../model/receipt.model';

@Injectable({
  providedIn: 'root',
})
export class FileGroupService {
  private readonly httpClient = inject(HttpClient);

  uploadFiles(receiptId: string, files: File[], fileType: FileType, isOriginal: boolean): Observable<void> {
    const body = new FormData();

    for (const file of files) {
      body.append('file', file, file.name);
    }

    body.append(
      'body',
      new File(
        [
          JSON.stringify({
            fileType,
            isOriginal,
          }),
        ],
        'body.json',
        {type: 'application/json'}
      )
    );

    return this.httpClient.post<void>(`/api/receipt-files/${receiptId}`, body);
  }
}
