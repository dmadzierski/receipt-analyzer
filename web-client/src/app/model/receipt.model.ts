export interface GetReceiptListResponse {
  items: GetReceiptListResponseItem[];
}

export interface GetReceiptListResponseItem {
  id: string;
  name: string;
  description: string;
  createDate: Date;
}

export interface GetReceiptDetailsResponse {
}

export class CreateReceiptData {
  constructor(
    public name: string,
    public description: string,
    public date: string,
    public files: File[]
  ) {
  }
}

export interface CreateReceiptBody {
  name: string;
  description: string;
  date: string;
}

export interface CreateReceiptResponse {
}

export interface GetReceiptDetailsResponse {
  id: string
  name: string
  description: string
  preferredRevision: RevisionDetails
  revisions: Revision[]
  createDate: string
  updateDate: string
}

export interface RevisionDetails {
  id: string
  resolver: any
  createdDate: string
  items: Item[]
  files: ReceiptFile[]
}

export interface Item {
  id: string
  name: string
  vat: string
  amount: number
  unitPrice: number
  discount?: number
  totalPrice: number
}

export interface ReceiptFile {
  id: string
  path: string
  rawData?: string
}

export interface Revision {
  id: string
  resolver: any
  createdDate: string
}
