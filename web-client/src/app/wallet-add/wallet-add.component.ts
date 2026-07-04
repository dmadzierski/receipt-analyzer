import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FileUploadComponent} from '../component/file-upload/file-upload.component';
import {FormsModule} from '@angular/forms';
import {WalletService} from '../service/wallet-service';
import {AddWalletData} from '../model/wallet.model';

@Component({
  selector: 'app-wallet-add',
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
  templateUrl: './wallet-add.component.html',
  styleUrl: './wallet-add.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WalletAddComponent {
  constructor(private readonly walletService: WalletService,) {
  }

  data: AddWalletData = new AddWalletData('');

  create() {
    console.log(this.data);
    this.walletService.addWallet(this.data).subscribe((res) => {
      console.log(res);
    });
  }
}
