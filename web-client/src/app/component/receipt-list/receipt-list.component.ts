import {Component, Input, OnInit} from '@angular/core';
import {ReceiptService} from '../../service/receipt.service';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterModule} from '@angular/router';
import {GetReceiptListResponse, GetReceiptResponseItem} from '../../model/receipt.model';

@Component({
  selector: 'receipt-list',
  templateUrl: './receipt-list.component.html',
  styleUrl: './receipt-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule],
})
export class ReceiptListComponent{
  displayedColumns: string[] = ['name', 'description', 'createDate'];

  @Input()
  public dataSource: GetReceiptResponseItem[] = [];

  constructor(
    private readonly router: Router
  ) {
  }

  goToDetails(row: GetReceiptResponseItem) {
    this.router.navigate(['/receipt/details', row.id]);
  }

}
