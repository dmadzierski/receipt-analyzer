export interface GetReceiptListResponse {
  items: GetReceiptResponseItem[];
}

export interface GetReceiptResponseItem {
  id: string;
  name: string;
  description: string;
  createdDate: Date;
}

export class CreateReceiptData {
  constructor(
    public walletId: string,
    public name: string,
    public description: string,
    public strategy: ResolverStrategy,
    public files: File[],
    public storeId: string = '',
  ) {
  }
}

export enum ResolverStrategy {
  BIEDRONKA,
  USER,
  BIEDRONKA_JSON
}

export interface CreateReceiptResponse {
}

export interface StoreDetails {
  id: string;
  brand: string;
  address: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface GetReceiptDetailsResponse {
  id: string
  name: string
  description: string
  preferredRevision?: RevisionDetails
  revisions: Revision[]
  createdDate: string
  updateDate: string,
  fileId: string
  store?: StoreDetails
}

export interface RevisionDetails {
  id: string
  resolver: any
  createdDate: string
  brand: string
  totalPrice: Number
  paymentDate: string
  address: string
  isPreferredRevision: boolean
  isCorrect: boolean
  items: Item[]
  files: ReceiptFile[]
}

export interface Item {
  id?: string
  position: number
  name: string
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
  brand: string
  totalPrice: Number
  paymentDate: string
  address: string
  isPreferredRevision: boolean
  isCorrect: boolean
}
