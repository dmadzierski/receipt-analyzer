import {WalletService} from '../service/wallet-service';
import {GetReceiptResponseItem} from './receipt.model';

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
  ) {}
}
export interface AddWalletResponse {
  id: string;
  name: string;
}

export interface WalletDetails {
  id: string;
  name: string;
  receipts: GetReceiptResponseItem[];
}
