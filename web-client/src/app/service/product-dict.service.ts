import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  GetProductDictListResponse,
  UpdateProductDictListRequest,
  UpdateProductDictListResponse
} from '../model/receipt-dict.mode';

@Injectable({
  providedIn: 'root',
})
export class ProductDictService {
  private readonly httpClient = inject(HttpClient);


  getProductDictList(): Observable<GetProductDictListResponse>{
    return this.httpClient.get<GetProductDictListResponse>(`/api/product-dicts`);
  }

  updateProductDictList(request: UpdateProductDictListRequest): Observable<UpdateProductDictListResponse> {
    return this.httpClient.post<UpdateProductDictListResponse>(`/api/product-dicts`, request);
  }
}
