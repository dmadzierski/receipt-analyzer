import {Routes} from '@angular/router';
import {ReceiptAddComponent} from './receipt-add/receipt-add.component';
import {ReceiptDetailsComponent} from './receipt-details/receipt-details.component';
import {WalletListComponent} from './wallet-list/wallet-list.component';
import {WalletAddComponent} from './wallet-add/wallet-add.component';
import {WalletDetailsComponent} from './wallet-details/wallet-details.component';

export const routes: Routes = [
  {
    path: 'receipts/details/:id',
    component: ReceiptDetailsComponent,
    pathMatch: 'prefix',
  },
  {
    path: 'wallets/:walletId',
    component: WalletDetailsComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallets/:walletId/receipt/add',
    component: ReceiptAddComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallets',
    component: WalletListComponent,
    pathMatch: 'full',
  },
  {
    path: '',
    redirectTo: 'wallets',
    pathMatch: 'full',
  },
  {
    path: 'wallets/new',
    component: WalletAddComponent,
    pathMatch: 'full',
  },
];
