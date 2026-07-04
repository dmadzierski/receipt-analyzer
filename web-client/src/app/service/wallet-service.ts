import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AddWalletData, AddWalletResponse, GetWalletListResponse} from '../model/wallet.model';

@Injectable({
  providedIn: 'root',
})
export class WalletService {


  private readonly httpClient = inject(HttpClient);

  getWalletList() {
    return this.httpClient.get<GetWalletListResponse>(
      '/api/wallets',
      {
        withCredentials: true
      }
    )
  }

  addWallet(data: AddWalletData) {
    return this.httpClient.post<AddWalletResponse>(
      '/api/wallets', data
    )
  }
}
