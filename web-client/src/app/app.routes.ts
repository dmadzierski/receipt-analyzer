import {Routes} from '@angular/router';
import {ReceiptAddComponent} from './receipt-add/receipt-add.component';
import {ReceiptDetailsComponent} from './receipt-details/receipt-details.component';
import {WalletListComponent} from './wallet-list/wallet-list.component';
import {WalletAddComponent} from './wallet-add/wallet-add.component';
import {WalletDetailsComponent} from './wallet-details/wallet-details.component';
import {ProductDictListComponent} from './product-dict-list/product-dict-list-component';
import {ProductCategoryListComponent} from './product-category-list/product-category-list.component';

export const routes: Routes = [
  {
    path: 'receipts/:id',
    component: ReceiptDetailsComponent,
    pathMatch: 'prefix',
  },
  {
    path: 'wallets/new',
    component: WalletAddComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallets/:walletId',
    component: WalletDetailsComponent,
    pathMatch: 'full',
  },
  {
    path: 'wallets/:walletId/receipts/new',
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
    path: 'product-dicts',
    component: ProductDictListComponent,
    pathMatch: 'full',
  },
  {
    path: 'product-categories',
    component: ProductCategoryListComponent,
    pathMatch: 'full',
  }
];
