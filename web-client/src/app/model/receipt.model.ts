export interface GetReceiptListResponse {
  items: GetReceiptResponseItem[];
}

export interface GetReceiptResponseItem {
  id: string;
  name: string;
  description: string;
  createDate: Date;
}

export interface GetReceiptDetailsResponse {
}

export class CreateReceiptData {
  constructor(
    public walletId: string,
    public name: string,
    public description: string,
    public date: string,
    public strategy: ResolverStrategy,
    public files: File[]
  ) {
  }
}

export enum ResolverStrategy{
  BIEDRONKA,
  USER,
  BIEDRONKA_JSON
}

export interface CreateReceiptResponse {
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
}

export interface RevisionDetails {
  id: string
  resolver: any
  createdDate: string
  brand: string
  totalPrice: Number
  payingDate: string
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
  payingDate: string
  address: string
  isPreferredRevision: boolean
  isCorrect: boolean
}
