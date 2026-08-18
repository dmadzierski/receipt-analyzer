import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  CreateProductCategoryRequest,
  CreateProductCategoryResponse,
  GetProductCategoryListResponse,
  UpdateProductCategoryRequest,
  UpdateProductCategoryResponse
} from '../model/product-category.model';

@Injectable({
  providedIn: 'root',
})
export class ProductCategoryService {
  private readonly httpClient = inject(HttpClient);

  getProductCategoryList(): Observable<GetProductCategoryListResponse> {
    return this.httpClient.get<GetProductCategoryListResponse>(`/api/product-categories`);
  }

  createProductCategory(request: CreateProductCategoryRequest): Observable<CreateProductCategoryResponse> {
    return this.httpClient.post<CreateProductCategoryResponse>(`/api/product-categories`, request);
  }

  updateProductCategory(id: string, request: UpdateProductCategoryRequest): Observable<UpdateProductCategoryResponse> {
    return this.httpClient.put<UpdateProductCategoryResponse>(`/api/product-categories/${id}`, request);
  }

  deleteProductCategory(id: string): Observable<void> {
    return this.httpClient.delete<void>(`/api/product-categories/${id}`);
  }
}
