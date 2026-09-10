export interface GetProductDictListResponse {
  items: ProductDict[];
}

export interface ProductDict {
  id: string;
  name: string;
  productCategoryId?: string | null;
  productCategoryIds?: string[] | null;
  productCategories?: ProductCategoryRef[] | null;
  productCategory?: ProductCategoryRef | null;
  aliases: Alias[];
}

export interface ProductCategoryRef {
  id: string;
  name: string;
}

export interface Alias {
  id: string;
  alias: string;
}

export interface UpdateProductDictListRequest {
  items: UpdateProductDictListRequestItem[];
}

export interface UpdateProductDictListRequestItem {
  canonicalName: string;
  productCategoryIds: string[];
  productDictList: string[];
}

export interface UpdateProductDictListResponse {
  items: UpdateProductDictListResponseItem[];
}

export interface UpdateProductDictListResponseItem {
  id: string;
  name: string;
  productCategoryId: string | null;
  aliases: Alias[];
}
