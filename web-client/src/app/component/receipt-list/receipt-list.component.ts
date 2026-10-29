import {Component, Input, ViewChild, AfterViewInit} from '@angular/core';
import {MatTableModule, MatTableDataSource} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSortModule, MatSort} from '@angular/material/sort';
import {Router, RouterModule} from '@angular/router';
import {WalletReceipt} from '../../model/wallet.model';
import {StoreDetails} from '../../model/receipt.model';
import {DatePipe, DecimalPipe} from '@angular/common';

@Component({
  selector: 'receipt-list',
  templateUrl: './receipt-list.component.html',
  styleUrl: './receipt-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule, DatePipe, DecimalPipe, MatSortModule],
})
export class ReceiptListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'store', 'paymentDate', 'totalPrice', 'isCorrect', 'createdDate'];
  matDataSource = new MatTableDataSource<WalletReceipt>();

  @ViewChild(MatSort) sort!: MatSort;

  @Input()
  public walletId: string = '';

  @Input()
  set dataSource(value: WalletReceipt[]) {
   this.matDataSource.data = value;
  }

  constructor(
   private readonly router: Router
  ) {
   this.matDataSource.sortingDataAccessor = (receipt, column) =>
     column === 'store' ? this.formatStore(receipt.store) : (receipt as any)[column] ?? '';
  }

  ngAfterViewInit() {
   this.sort.active = 'createdDate';
   this.sort.direction = 'desc';
   this.matDataSource.sort = this.sort;
  }

  goToDetails(row: WalletReceipt) {
   this.router.navigate(['/receipts/', row.id]);
  }

  formatStore(store?: StoreDetails): string {
   if (!store) {
     return '';
   }
   return [store.brand, store.address, store.city].filter(Boolean).join(', ');
  }

}
