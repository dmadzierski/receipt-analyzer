import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {provideNativeDateAdapter} from '@angular/material/core';
import {WalletDetails} from '../model/wallet.model';
import {WalletService} from '../service/wallet-service';
import {ReceiptListComponent} from '../component/receipt-list/receipt-list.component';
import {GetReceiptResponseItem} from '../model/receipt.model';


@Component({
  selector: 'app-wallet-details',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatDatepickerModule,
    MatButtonModule,
    ReceiptListComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './wallet-details.component.html',
  styleUrl: './wallet-details.component.scss',
})
export class WalletDetailsComponent implements OnInit {
  constructor(
    private readonly walletService: WalletService,
    private readonly route: ActivatedRoute
  ) {
  }

  public wallet: WalletDetails = {
    id: '',
    name: '',
    receipts: []
  };

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const walletId = params.get('walletId');
      if (walletId != null) {
        this.walletService.getWalletDetails(walletId).subscribe((res) => {
          this.wallet = res;
        });
      }
    })
  }

  getReceipts(): GetReceiptResponseItem[] {
    return this.wallet?.receipts || [];
  }
}
