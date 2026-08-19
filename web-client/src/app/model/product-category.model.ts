export interface GetProductCategoryListResponse {
  items: ProductCategory[];
}

export interface ProductCategory {
  id: string;
  name: string;
}

export interface CreateProductCategoryRequest {
  name: string;
}

export interface CreateProductCategoryResponse {
  id: string;
  name: string;
}

export interface UpdateProductCategoryRequest {
  name: string;
}

export interface UpdateProductCategoryResponse {
  id: string;
  name: string;
}
