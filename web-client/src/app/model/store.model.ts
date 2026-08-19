export interface GetStoreListResponse {
  items: GetStoreListResponseItem[];
}

export interface GetStoreListResponseItem {
  id: string;
  brand: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface GetStoreBrandListResponse {
  storeBrands: StoreBrandItem[];
}

export interface StoreBrandItem {
  id: string;
  name: string;
}

export interface CreateStoreRequest {
  storeBrandId?: string | null;
  brandName: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface CreateStoreResponse {
  id: string;
  brand: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
}
