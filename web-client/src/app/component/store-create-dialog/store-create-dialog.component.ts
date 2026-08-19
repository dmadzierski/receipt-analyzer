import {Component, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatAutocompleteModule, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {debounceTime, distinctUntilChanged, map, of, Subject, switchMap} from 'rxjs';
import {StoreService} from '../../service/store.service';
import {CreateStoreRequest, CreateStoreResponse, StoreBrandItem} from '../../model/store.model';

@Component({
  selector: 'app-store-create-dialog',
  standalone: true,
  templateUrl: './store-create-dialog.component.html',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule,
  ],
})
export class StoreCreateDialogComponent {
  @ViewChild(MatAutocompleteTrigger) brandAutocompleteTrigger?: MatAutocompleteTrigger;

  model: CreateStoreRequest = {
    storeBrandId: null,
    brandName: '',
    address: '',
    city: '',
    postalCode: '',
    country: '',
  };
  brandSearchText = '';
  brandOptions: StoreBrandItem[] = [];
  private readonly brandSearchSubject = new Subject<string>();

  constructor(
    private readonly dialogRef: MatDialogRef<StoreCreateDialogComponent>,
    private readonly storeService: StoreService,
  ) {
    this.brandSearchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query: string) => {
          if (query.length < 3) {
            this.brandOptions = [];
            return of([] as StoreBrandItem[]);
          }

          return this.storeService.getStoreBrands(query).pipe(
            map((result) => result.storeBrands ?? [])
          );
        })
      )
      .subscribe((brands) => {
        this.brandOptions = brands as StoreBrandItem[];
      });
  }

  searchBrands(): void {
    const query = this.brandSearchText.trim();
    if (query.length < 3) {
      this.brandOptions = [];
      this.brandAutocompleteTrigger?.closePanel();
      return;
    }

    this.model.brandName = query;
    this.model.storeBrandId = null;
    this.brandSearchSubject.next(query);
    this.brandAutocompleteTrigger?.openPanel();
  }

  selectBrand(brand: StoreBrandItem): void {
    this.model.storeBrandId = brand.id;
    this.model.brandName = brand.name;
    this.brandSearchText = brand.name;
  }

  save(): void {
    const brandName = this.model.brandName?.trim();
    if (!brandName || !this.model.address.trim() || !this.model.city.trim()) {
      return;
    }

    this.model.brandName = brandName;
    this.storeService.createStore(this.model).subscribe((createdStore: CreateStoreResponse) => {
      this.dialogRef.close(createdStore);
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
