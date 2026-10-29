import {StoreDetails} from './receipt.model';

export interface GetWalletListResponse {
  items: GetWalletListResponseItem[];
}

export interface GetWalletListResponseItem {
  id: string;
  name: string;
}

export class AddWalletData {
  constructor(
    public name: string,
  ) {
  }
}

export interface AddWalletResponse {
  id: string;
  name: string;
}

export interface WalletDetails {
  id: string;
  name: string;
  receipts: WalletReceipt[];
}

export interface WalletReceipt {
  id: string;
  name: string;
  paymentDate?: string;
  isCorrect?: boolean;
  totalPrice?: number;
  createdDate: string;
  store?: StoreDetails;
}
