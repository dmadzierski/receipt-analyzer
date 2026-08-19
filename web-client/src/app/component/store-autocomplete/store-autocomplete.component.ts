import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatAutocompleteModule, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {GetStoreListResponseItem} from '../../model/store.model';

@Component({
  selector: 'app-store-autocomplete',
  standalone: true,
  templateUrl: './store-autocomplete.component.html',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
  ],
})
export class StoreAutocompleteComponent {
  @Input() label = 'Store';
  @Input() value = '';
  @Input() options: GetStoreListResponseItem[] = [];
  @Input() minLength = 3;
  @Output() valueChange = new EventEmitter<string>();
  @Output() optionSelected = new EventEmitter<GetStoreListResponseItem>();
  @Output() createNew = new EventEmitter<string>();

  @ViewChild(MatAutocompleteTrigger) autocompleteTrigger?: MatAutocompleteTrigger;

  openPanel(): void {
    this.autocompleteTrigger?.openPanel();
  }

  closePanel(): void {
    this.autocompleteTrigger?.closePanel();
  }

  onInputChange(value: string): void {
    const trimmed = value.trim();
    this.value = trimmed;
    this.valueChange.emit(trimmed);

    if (trimmed.length >= this.minLength) {
      this.autocompleteTrigger?.openPanel();
    } else {
      this.autocompleteTrigger?.closePanel();
    }
  }

  selectOption(store: GetStoreListResponseItem): void {
    this.value = `${store.brand} - ${store.city} ${store.address}`;
    this.valueChange.emit(this.value);
    this.optionSelected.emit(store);
  }

  displayValue(store: GetStoreListResponseItem): string {
    return `${store.brand} - ${store.city} ${store.address}`;
  }
}
