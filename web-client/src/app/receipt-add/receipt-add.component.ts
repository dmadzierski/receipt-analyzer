import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FileUploadComponent} from '../component/file-upload/file-upload.component';
import {ReceiptService} from '../service/receipt.service';
import {CreateReceiptData, ResolverStrategy} from '../model/receipt.model';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-receipt-add',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatDatepickerModule,
    MatButtonModule,
    FileUploadComponent,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './receipt-add.component.html',
  styleUrl: './receipt-add.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReceiptAddComponent {

  private walletId: string = '';

  constructor(private readonly receiptService: ReceiptService,
              private readonly activatedRoute: ActivatedRoute,
  ) {
    this.activatedRoute.params.subscribe(params => {
      this.walletId = params['walletId'];
    })
  }


  strategies = Object.keys(ResolverStrategy).filter(key => Number.isNaN(Number(key)));
  data: CreateReceiptData = new CreateReceiptData('', '', '', '', ResolverStrategy.BIEDRONKA, []);

  create() {
    console.log(this.data);
    this.data.walletId = this.walletId;
    this.receiptService.addReceipt(this.data).subscribe((res) => {
      console.log(res);
    });
  }
}
