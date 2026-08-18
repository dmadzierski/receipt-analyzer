import {Component, Input, ViewChild, AfterViewInit} from '@angular/core';
import {MatTableModule, MatTableDataSource} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSortModule, MatSort} from '@angular/material/sort';
import {Router, RouterModule} from '@angular/router';
import {GetReceiptResponseItem} from '../../model/receipt.model';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'receipt-list',
  templateUrl: './receipt-list.component.html',
  styleUrl: './receipt-list.component.scss',
  imports: [MatTableModule, MatIconModule, MatButtonModule, RouterModule, DatePipe, MatSortModule],
})
export class ReceiptListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'description', 'createdDate'];
  matDataSource = new MatTableDataSource<GetReceiptResponseItem>();

  @ViewChild(MatSort) sort!: MatSort;

  @Input()
  public walletId: string = '';

  @Input()
  set dataSource(value: GetReceiptResponseItem[]) {
   this.matDataSource.data = value;
  }

  constructor(
   private readonly router: Router
  ) {
  }

  ngAfterViewInit() {
   this.sort.active = 'createdDate';
   this.sort.direction = 'desc';
   this.matDataSource.sort = this.sort;
  }

  goToDetails(row: GetReceiptResponseItem) {
   this.router.navigate(['/receipts/', row.id]);
  }

}
