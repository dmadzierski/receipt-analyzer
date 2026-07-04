import {Routes} from '@angular/router';
import {ReceiptListComponent} from './receipt-list/receipt-list.component';
import {ReceiptAddComponent} from './receipt-add/receipt-add.component';
import {ReceiptDetailsComponent} from './receipt-details/receipt-details.component';
import {WalletListComponent} from './wallet-list/wallet-list.component';
import {WalletAddComponent} from './wallet-add/wallet-add.component';

export const routes: Routes = [
  {
    path: 'receipt/details/:id',
    component: ReceiptDetailsComponent,
    pathMatch: 'prefix',
  },
  {
    path: 'wallet/:walletId/receipt/list',
    component: ReceiptListComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallet/:walletId/receipt/add',
    component: ReceiptAddComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallet/list',
    component: WalletListComponent,
    pathMatch: 'full',
  },
  {
    path: '',
    redirectTo: 'wallet/list',
    pathMatch: 'full',
  },
  {
    path: 'wallet/add',
    component: WalletAddComponent,
    pathMatch: 'full',
  },
];
