import {CommonModule} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatTableModule} from '@angular/material/table';
import {GetStoreListResponseItem} from '../model/store.model';
import {StoreService} from '../service/store.service';

@Component({
  selector: 'app-store-list',
  templateUrl: './store-list.component.html',
  styleUrl: './store-list.component.scss',
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatTableModule],
})
export class StoreListComponent implements OnInit {
  dataSource: GetStoreListResponseItem[] = [];
  searchText = '';
  displayedColumns: string[] = ['brand', 'address', 'city', 'postalCode', 'country'];

  constructor(private readonly storeService: StoreService) {
  }

  ngOnInit(): void {
    this.loadStores();
  }

  search(): void {
    this.loadStores();
  }

  loadStores(): void {
    this.storeService.getStoreList(this.searchText.trim()).subscribe((result) => {
      this.dataSource = result.items ?? [];
    });
  }
}
