import {Component, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {Revision} from '../../model/receipt.model';
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
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {MatCheckbox} from '@angular/material/checkbox';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'app-revision-list',
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatTable,
    MatTableModule, MatIconModule, MatButtonModule, RouterModule, MatSortHeader, MatSort, MatCheckbox, DatePipe
  ],
  templateUrl: './revision-list.component.html',
  styleUrl: './revision-list.component.scss',
})
export class RevisionListComponent implements OnChanges {
  constructor() {
  }

  displayedColumns: string[] = ['brand', 'resolver', 'createdDate', 'totalPrice', 'payingDate', 'address', 'preferredRevision', 'isCorrect'];

  @Input()
  revisions: Revision[] = {} as Revision[];

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.data.sort = sort;
    }
  }

  data = new MatTableDataSource({} as Revision[]);

  ngOnChanges(changes: SimpleChanges) {
    if (changes['revisions'] && this.revisions) {
      this.data.data = this.revisions;
    }
  }

}


