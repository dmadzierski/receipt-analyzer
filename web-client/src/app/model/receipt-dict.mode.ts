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
