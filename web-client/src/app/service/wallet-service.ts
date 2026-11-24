import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs';
import {
  AddWalletData,
  AddWalletResponse,
  GetWalletListResponse,
  GetWalletListResponseItem,
  WalletDetails
} from '../model/wallet.model';

@Injectable({
  providedIn: 'root',
})
export class WalletService {

  private readonly httpClient = inject(HttpClient);
  private readonly walletList = signal<GetWalletListResponseItem[]>([]);

  readonly wallets = this.walletList.asReadonly();

  loadWallets() {
    this.httpClient.get<GetWalletListResponse>(
      '/api/wallets',
      {
        withCredentials: true
      }
    ).subscribe((result) => this.walletList.set(result.items ?? []));
  }

  addWallet(data: AddWalletData) {
    return this.httpClient.post<AddWalletResponse>(
      '/api/wallets', data
    ).pipe(tap(() => this.loadWallets()));
  }

  getWalletDetails(walletId: string) {
    return this.httpClient.get<WalletDetails>(
      `/api/wallets/${walletId}`
    )

  }
}
