import {Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
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
import {RevisionService} from '../../service/revision.service';
import {ReceiptService} from '../../service/receipt.service';

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
  displayedColumns: string[] = ['selected', 'resolver', 'createdDate', 'totalPrice', 'paymentDate', 'isPreferredRevision', 'isCorrect', 'actions'];
  @Input()
  revisions: Revision[] = {} as Revision[];
  @Input()
  receiptId: string = '';
  @Output() selectedRevisionChange = new EventEmitter<string>();
  public selectedId: string | undefined = 'init';
  data = new MatTableDataSource({} as Revision[]);
  private readonly revisionService = inject(RevisionService);
  private readonly receiptService = inject(ReceiptService);

  constructor() {
  }

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.data.sort = sort;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['revisions'] && this.revisions) {
      this.data.data = this.revisions;
    }
    if (this.selectedId === 'init' && this.revisions != undefined) {
      this.selectedId = this.revisions?.find(revision => revision?.isPreferredRevision)?.id;
    }
  }

  duplicateRevision(id: string) {
    if (!id) return;
    this.revisionService.copyRevision(id).subscribe({
      next: () => {
        this.refreshData(this.receiptId)
      },
      error: (err) => console.error(err)
    });
  }

  refreshData(receiptId: string) {
    this.receiptService.getReceiptRevisions(receiptId).subscribe(
      {
        next: (revisions) => {
          this.data.data = revisions;
        }
      }
    )
  }

  changeSelected(revisionId: string) {
    this.selectedId = (this.selectedId === revisionId) ? undefined : revisionId;
    this.selectedRevisionChange.emit(this.selectedId);
  }
}


