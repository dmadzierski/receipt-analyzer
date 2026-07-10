import {Component} from '@angular/core';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable
} from '@angular/material/table';
import {Router} from '@angular/router';
import {GetReceiptResponseItem} from '../model/receipt.model';
import {GetProductDictListResponse, ProductDict} from '../model/receipt-dict.mode';
import {ProductDictService} from '../service/product-dict.service';

@Component({
  selector: 'app-product-dict-list',
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable
  ],
  templateUrl: './product-dict-list-component.html',
  styleUrl: './product-dict-list-component.scss',
})
export class ProductDictListComponent {
  dataSource: ProductDict[] = [];
  displayedColumns: string[] = ['name', 'aliases'];

  constructor(
    private readonly productDictService: ProductDictService,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    this.getWalletList();
  }

  private getWalletList() {
    this.productDictService
      .getProductDictList()
      .subscribe((result: GetProductDictListResponse) => {
        this.dataSource = result.items;
      });
  }

  toReceipts(row: GetReceiptResponseItem) {
    this.router.navigate(['/wallets/', row.id]);
  }
}
