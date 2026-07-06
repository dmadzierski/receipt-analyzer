import {Component, OnInit} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterModule} from '@angular/router';
import {GetReceiptResponseItem} from '../model/receipt.model';
import {GetWalletListResponse, GetWalletListResponseItem} from '../model/wallet.model';
import {WalletService} from '../service/wallet-service';

@Component({
  selector: 'wallet-list',
  templateUrl: './wallet-list.component.html',
  styleUrl: './wallet-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule],
})
export class WalletListComponent implements OnInit {
  dataSource: GetWalletListResponseItem[] = [];
  displayedColumns: string[] = ['name'];

  constructor(
    private readonly walletService: WalletService,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    this.getWalletList();
  }

  private getWalletList() {
    this.walletService
      .getWalletList()
      .subscribe((result: GetWalletListResponse) => {
        this.dataSource = result.items;
      });
  }

  toReceipts(row: GetReceiptResponseItem) {
    this.router.navigate(['/wallets/', row.id]);
  }
}
