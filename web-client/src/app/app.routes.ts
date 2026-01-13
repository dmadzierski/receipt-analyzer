import {Routes} from '@angular/router';
import {ReceiptListComponent} from './receipt-list/receipt-list.component';
import {ReceiptAddComponent} from './receipt-add/receipt-add.component';
import {ReceiptDetailsComponent} from './receipt-details/receipt-details.component';

export const routes: Routes = [
  {
    path: 'receipt/details/:id',
    component: ReceiptDetailsComponent,
    pathMatch: 'prefix',
  },
  {
    path: 'receipt/list',
    component: ReceiptListComponent,
    pathMatch: 'full',
  },
  {
    path: 'receipt/add',
    component: ReceiptAddComponent,
    pathMatch: 'full',
  },
  {
    path: '',
    redirectTo: 'receipt/list',
    pathMatch: 'full',
  },
];
