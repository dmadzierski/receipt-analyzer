export interface GetProductCategoryListResponse {
  items: ProductCategory[];
}

export interface ProductCategory {
  id: string;
  version: number;
  name: string;
}

export interface CreateProductCategoryRequest {
  name: string;
}

export interface CreateProductCategoryResponse {
  id: string;
}

export interface UpdateProductCategoryRequest {
  version: number;
  name: string;
}

export interface UpdateProductCategoryResponse {
  id: string;
}
