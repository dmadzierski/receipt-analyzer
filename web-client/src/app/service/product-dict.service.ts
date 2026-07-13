import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  GetProductDictListResponse,
  UpdateProductDictListRequest
} from '../model/receipt-dict.mode';

@Injectable({
  providedIn: 'root',
})
export class ProductDictService {
  private readonly httpClient = inject(HttpClient);


  getProductDictList(): Observable<GetProductDictListResponse>{
    return this.httpClient.get<GetProductDictListResponse>(`/api/product-dicts`);
  }

  updateProductDictList(request: UpdateProductDictListRequest): Observable<void> {
    return this.httpClient.post<void>(`/api/product-dicts`, request);
  }
}
