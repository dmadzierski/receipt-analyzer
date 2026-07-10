export interface GetProductDictListResponse {
  items: ProductDict[];
}

export interface ProductDict {
  id: string;
  name: string;
  aliases: Alias[];
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
  productDictList: string[];
}

export interface UpdateProductDictListResponse {
  items: UpdateProductDictListResponseItem[];
}

export interface UpdateProductDictListResponseItem {
  id: string;
  name: string;
  aliases: Alias[];
}
