export interface GetReceiptListResponse {
  items: GetReceiptListResponseItem[];
}

export interface GetReceiptListResponseItem {
  id: string;
  name: string;
  description: string;
  createDate: Date;
}
export interface GetReceiptDetailsResponse {}

export class CreateReceiptData {
  constructor(
    public name: string,
    public description: string,
    public date: string,
    public files: File[]
  ) {}
}

export interface CreateReceiptBody {
  name: string;
  description: string;
  date: string;
}

export interface CreateReceiptResponse {}

export interface GetReceiptDetailsResponse {
  id: string;
  name: string;
  description: string;
  preferredRevision: GetReceiptDetailsRevisionDetailsResponse;
  revisions: GetReceiptDetailsRevisionResponse[];
}

export interface GetReceiptDetailsRevisionDetailsResponse {
  id: string;
  brand: string;
  receiptFiles: GetReceiptDetailsFileResponse[];
  items: GetReceiptDetailsItemResponse[];
  totalPrice: Number;
  payingDate: string;
  address: string;
}

export interface GetReceiptDetailsFileResponse {
  id: string;
}

export interface GetReceiptDetailsItemResponse {
  id: string;
  name: string;
  vat: string;
  amount: Number;
  unitPrice: Number;
  discount: Number;
  totalPrice: Number;
}

export interface GetReceiptDetailsRevisionResponse {
  id: string;
  resolver: string;
  createdDate: Date;
}
