import {Component, Input, OnInit} from '@angular/core';
import {ReceiptService} from '../../service/receipt.service';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterModule} from '@angular/router';
import {GetReceiptListResponse, GetReceiptResponseItem} from '../../model/receipt.model';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'receipt-list',
  templateUrl: './receipt-list.component.html',
  styleUrl: './receipt-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule, DatePipe],
})
export class ReceiptListComponent{
  displayedColumns: string[] = ['name', 'description', 'createDate'];

  @Input()
  public walletId: string = '';

  @Input()
  public dataSource: GetReceiptResponseItem[] = [];

  constructor(
    private readonly router: Router
  ) {
  }

  goToDetails(row: GetReceiptResponseItem) {
    this.router.navigate(['/receipts/', row.id]);
  }

}
