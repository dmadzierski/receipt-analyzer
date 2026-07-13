import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {WalletService} from '../service/wallet-service';
import {AddWalletData} from '../model/wallet.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-wallet-add',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './wallet-add.component.html',
  styleUrl: './wallet-add.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WalletAddComponent {
  constructor(
    private readonly walletService: WalletService,
    private readonly router: Router,
    ) {
  }

  data: AddWalletData = new AddWalletData('');

  create() {
    console.log(this.data);
    this.walletService.addWallet(this.data).subscribe((res) => {
      console.log(res);
      this.router.navigate(['/']);
    });
  }
}
