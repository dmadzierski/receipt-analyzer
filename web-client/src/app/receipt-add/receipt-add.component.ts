import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDialog} from '@angular/material/dialog';
import {FileUploadComponent} from '../component/file-upload/file-upload.component';
import {StoreAutocompleteComponent} from '../component/store-autocomplete/store-autocomplete.component';
import {ReceiptService} from '../service/receipt.service';
import {StoreService} from '../service/store.service';
import {CreateReceiptData, ResolverStrategy} from '../model/receipt.model';
import {CreateStoreResponse, GetStoreListResponseItem} from '../model/store.model';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {debounceTime, distinctUntilChanged, map, of, Subject, switchMap} from 'rxjs';
import {StoreCreateDialogComponent} from '../component/store-create-dialog/store-create-dialog.component';

@Component({
  selector: 'app-receipt-add',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatButtonModule,
    FileUploadComponent,
    StoreAutocompleteComponent,
  ],
  templateUrl: './receipt-add.component.html',
  styleUrl: './receipt-add.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReceiptAddComponent {

  @ViewChild(StoreAutocompleteComponent) storeAutocomplete?: StoreAutocompleteComponent;

  strategies = Object.keys(ResolverStrategy).filter(key => Number.isNaN(Number(key)));
  stores: GetStoreListResponseItem[] = [];
  data: CreateReceiptData = new CreateReceiptData('', '', '', ResolverStrategy.BIEDRONKA, []);
  storeSearchText = '';
  private readonly storeSearchSubject = new Subject<string>();
  private walletId: string = '';

  constructor(private readonly receiptService: ReceiptService,
              private readonly storeService: StoreService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly dialog: MatDialog,
  ) {
    this.activatedRoute.params.subscribe(params => {
      this.walletId = params['walletId'];
    });

    this.storeSearchSubject
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((query: string): any => {
          if (query.length < 3) {
            this.stores = [];
            return of([] as GetStoreListResponseItem[]);
          }

          return this.storeService.getStoreList(query).pipe(
            map((result) => result.items ?? [])
          );
        })
      )
      .subscribe((items) => {
        const storeList = items as GetStoreListResponseItem[];
        this.stores = storeList;
        if (this.stores.length > 0) {
          this.storeAutocomplete?.openPanel();
        } else {
          this.storeAutocomplete?.closePanel();
        }

        if (!this.data.storeId && this.stores.length > 0) {
          this.data.storeId = this.stores[0].id;
          this.storeSearchText = `${this.stores[0].brand} - ${this.stores[0].city} ${this.stores[0].address}`;
        }
      });
  }

  searchStores(): void {
    const query = this.storeSearchText.trim();
    if (query.length < 3) {
      this.stores = [];
      this.storeAutocomplete?.closePanel();
      return;
    }

    this.storeSearchSubject.next(query);
    this.storeAutocomplete?.openPanel();
  }

  selectStore(store: GetStoreListResponseItem): void {
    this.data.storeId = store.id;
    this.storeSearchText = `${store.brand} - ${store.city} ${store.address}`;
  }

  openCreateStoreDialog(): void {
    const dialogRef = this.dialog.open(StoreCreateDialogComponent, {
      width: '500px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((createdStore?: CreateStoreResponse) => {
      if (createdStore) {
        this.data.storeId = createdStore.id;
        this.storeSearchText = `${createdStore.brand} - ${createdStore.city} ${createdStore.address}`;
        this.stores = [];
        this.searchStores();
      }
    });
  }

  private loadStores(query: string = ''): void {
    this.storeSearchSubject.next(query);
  }

  create() {
    console.log(this.data);
    this.data.walletId = this.walletId;
    this.receiptService.addReceipt(this.data).subscribe((res) => {
      console.log(res);
    });
  }
}
