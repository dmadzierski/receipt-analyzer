import {Component, inject} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterModule} from '@angular/router';
import {GetWalletListResponseItem} from '../model/wallet.model';
import {WalletService} from '../service/wallet-service';

@Component({
  selector: 'wallet-list',
  templateUrl: './wallet-list.component.html',
  styleUrl: './wallet-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule],
})
export class WalletListComponent {
  private readonly router = inject(Router);

  readonly wallets = inject(WalletService).wallets;
  displayedColumns: string[] = ['name'];

  toReceipts(row: GetWalletListResponseItem) {
    this.router.navigate(['/wallets/', row.id]);
  }
}
