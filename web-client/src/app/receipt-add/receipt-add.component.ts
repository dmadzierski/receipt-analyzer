import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FileUploadComponent} from '../component/file-upload/file-upload.component';
import {ReceiptService} from '../service/receipt.service';
import {CreateReceiptData} from '../model/receipt.model';
import {FormsModule} from '@angular/forms';

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
  constructor(private readonly receiptService: ReceiptService) {
  }

  data: CreateReceiptData = new CreateReceiptData('', '', '', []);

  create() {
    console.log(this.data);
    this.receiptService.addReceipt(this.data).subscribe((res) => {
      console.log(res);
    });
  }
}
