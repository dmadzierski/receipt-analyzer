import {AfterViewInit, Component, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {Item, RevisionDetails} from '../../model/receipt.model';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatTable,
  MatTableDataSource,
  MatTableModule
} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {RouterModule} from '@angular/router';
import {MatSort, MatSortHeader, Sort} from '@angular/material/sort';

@Component({
  selector: 'app-revision-details',
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatTable,
    MatTableModule, MatIconModule, MatButtonModule, RouterModule, MatSortHeader, MatSort
  ],
  templateUrl: './revision-details.component.html',
  styleUrl: './revision-details.component.scss',
})
export class RevisionDetailsComponent implements OnChanges{
  constructor() {
  }

  displayedColumns: string[] = ['position', 'name', 'ptu', 'amount', 'unitPrice', 'totalPrice'];

  @Input()
  revision: RevisionDetails = {} as RevisionDetails

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.data.sort = sort;
    }
  }

  data = new MatTableDataSource({} as Item[]);

  ngOnChanges(changes: SimpleChanges) {
    if (changes['revision'] && this.revision?.items) {
      this.data.data = this.revision.items;
    }
  }

}


