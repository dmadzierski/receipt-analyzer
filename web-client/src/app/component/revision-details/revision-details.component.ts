import {ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
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
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {MatFormField, MatHint, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {CommonModule, NgClass} from '@angular/common';
import {CdkDragDrop, DragDropModule, moveItemInArray} from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-revision-details',
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    CommonModule,
    DragDropModule,
    MatTable,
    DragDropModule,
    MatTableModule, MatIconModule, MatButtonModule, RouterModule, MatSortHeader, MatSort, MatFormField, MatInput, MatLabel, ReactiveFormsModule, FormsModule, MatDatepicker, MatDatepickerInput, MatHint, MatDatepickerToggle, MatSuffix, NgClass
  ],
  templateUrl: './revision-details.component.html',
  styleUrl: './revision-details.component.scss',
})
export class RevisionDetailsComponent implements OnChanges {
  constructor(private cdr: ChangeDetectorRef) {
  }

  displayedColumns: string[] = ['position', 'name', 'ptu', 'amount', 'unitPrice', 'totalPrice', 'actions'];

  @Input()
  revision: RevisionDetails = {} as RevisionDetails

  @Input()
  contentEditable: boolean = false;

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.data.sort = sort;

      if (!this.contentEditable) {
        this.data.sort.active = 'position';
        this.data.sort.direction = 'asc';
      }
      this.cdr.detectChanges();
    }
  }

  data = new MatTableDataSource({} as Item[]);

  ngOnChanges(changes: SimpleChanges) {
    if (changes['revision'] && this.revision?.items) {
      this.data.data = [...this.revision.items].sort((a, b) => a.position - b.position);
    }

    if (changes['contentEditable']) {
      if (this.contentEditable) {
        this.data.sort = null;
        this.data.data = [...this.data.data].sort((a, b) => a.position - b.position);

      } else {
        if (this.data.sort) {
          this.data.sort = this.data.sort;
          this.data.data = [...this.data.data].sort((a, b) => a.position - b.position);
        }
      }
    }
  }

  drop(event: CdkDragDrop<Item[]>) {
    moveItemInArray(this.data.data, event.previousIndex, event.currentIndex);

    this.data.data = [...this.data.data];

    this.updatePositions();
  }

  addRowAfter(index: number) {
    const newItem: Item = {
      id: '',
      position: 0,
      name: '',
      ptu: '',
      amount: 0,
      unitPrice: 0,
      totalPrice: 0
    };

    this.data.data.splice(index + 1, 0, newItem);

    this.data.data = [...this.data.data];

    this.updatePositions();
  }

  removeRow(index: number) {
    this.data.data.splice(index, 1);
    this.data.data = [...this.data.data];
    this.updatePositions();
  }

  private updatePositions() {
    this.data.data.forEach((item, index) => {
      item.position = index + 1;
    });
  }
}


