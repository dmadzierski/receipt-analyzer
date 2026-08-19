import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  CreateStoreRequest,
  CreateStoreResponse,
  GetStoreBrandListResponse,
  GetStoreListResponse,
} from '../model/store.model';

@Injectable({
  providedIn: 'root',
})
export class StoreService {
  private readonly httpClient = inject(HttpClient);

  getStoreList(query: string = ''): Observable<GetStoreListResponse> {
    return this.httpClient.get<GetStoreListResponse>('/api/stores', {
      params: {query},
      withCredentials: true,
    });
  }

  getStoreBrands(query: string = ''): Observable<GetStoreBrandListResponse> {
    return this.httpClient.get<GetStoreBrandListResponse>('/api/store-brands', {
      params: {query},
      withCredentials: true,
    });
  }

  createStore(data: CreateStoreRequest): Observable<CreateStoreResponse> {
    return this.httpClient.post<CreateStoreResponse>('/api/stores', data, {
      withCredentials: true,
    });
  }
}
