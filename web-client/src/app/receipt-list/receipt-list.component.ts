import {Component, OnInit} from '@angular/core';
import {ReceiptService} from '../service/receipt.service';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterModule} from '@angular/router';
import {GetReceiptListResponse, GetReceiptListResponseItem} from '../model/receipt.model';

@Component({
  selector: 'receipt-list',
  templateUrl: './receipt-list.component.html',
  styleUrl: './receipt-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule],
})
export class ReceiptListComponent implements OnInit {
  dataSource: GetReceiptListResponseItem[] = [];
  displayedColumns: string[] = ['name', 'description', 'createDate'];

  constructor(
    private readonly receiptService: ReceiptService,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    this.getReceiptList();
  }

  private getReceiptList() {
    this.receiptService
      .getReceiptList()
      .subscribe((result: GetReceiptListResponse) => {
        this.dataSource = result.items;
      });
  }

  goToDetails(row: GetReceiptListResponseItem) {
    this.router.navigate(['/receipt/details', row.id]);
  }
}
