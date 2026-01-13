import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  CreateReceiptData,
  CreateReceiptResponse,
  GetReceiptDetailsResponse,
  GetReceiptListResponse,
} from '../model/receipt.model';

@Injectable({
  providedIn: 'root',
})
export class ReceiptService {
  private readonly httpClient = inject(HttpClient);

  getReceiptList(): Observable<GetReceiptListResponse> {
    return this.httpClient.get<GetReceiptListResponse>('/api/receipts/list', {
      withCredentials: true,
    });
  }

  getReceiptDetails(id: string): Observable<GetReceiptDetailsResponse> {
    return this.httpClient.get<GetReceiptDetailsResponse>(
      '/api/receipts/' + id,
      {
        withCredentials: true,
      }
    );
  }

  addReceipt(data: CreateReceiptData): Observable<CreateReceiptResponse> {
    let body = new FormData();

    for (const file of data.files) {
      body.append('file', file, file.name);
    }

    body.append(
      'body',
      new File(
        [
          JSON.stringify({
            name: data.name,
            description: data.description,
            date: data.date,
          }),
        ],
        'payapp_init_json.json',
        {type: 'application/json'}
      )
    );

    return this.httpClient.post<CreateReceiptResponse>('/api/receipts', body);
  }
}
