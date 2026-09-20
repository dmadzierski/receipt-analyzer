import {Component, OnInit} from '@angular/core';
import {ReceiptService} from '../service/receipt.service';
import {FileGroup, GetReceiptDetailsResponse, Revision, RevisionDetails} from '../model/receipt.model';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDialog} from '@angular/material/dialog';
import {provideNativeDateAdapter} from '@angular/material/core';
import {RevisionDetailsComponent} from '../component/revision-details/revision-details.component';
import {PdfViewerComponent} from '../component/pdf-viewer/pdf-viewer.component';
import {RevisionListComponent} from '../component/revision-list/revision-list.component';
import {MatIcon} from '@angular/material/icon';
import {RevisionService} from '../service/revision.service';
import {Observable} from 'rxjs';
import {MatTooltip} from '@angular/material/tooltip';
import {FileGroupSelectorComponent} from '../component/file-group-selector/file-group-selector.component';
import {AddFilesDialogComponent} from '../component/add-files-dialog/add-files-dialog.component';


@Component({
  selector: 'app-receipt-details',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatDatepickerModule,
    MatButtonModule,
    RevisionDetailsComponent,
    PdfViewerComponent,
    RevisionListComponent,
    MatIcon,
    MatTooltip,
    FileGroupSelectorComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './receipt-details.component.html',
  styleUrl: './receipt-details.component.scss',
})
export class ReceiptDetailsComponent implements OnInit {
  receiptDetails = {} as GetReceiptDetailsResponse;
  receiptFileId: string = '';
  fileGroups: FileGroup[] = [];
  selectedFileGroupId: string = '';
  editMode: boolean = false;
  receiptId: string | null = null;

  constructor(
    private readonly receiptService: ReceiptService,
    private readonly revisionService: RevisionService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.receiptId = this.route.snapshot.paramMap.get('id');
    if (this.receiptId) {
      this.receiptService.getReceiptDetails(this.receiptId).subscribe({
        next: (res) => {
          this.receiptDetails = res;
          this.fileGroups = res.fileGroups ?? [];
          this.selectPdfFile();
        }, error: () => {
          this.router.navigate(["/"]);
        }
      });
    }
  }

  selectPdfFile(fileGroupId?: string): void {
    const preferredGroup = fileGroupId
      ? this.fileGroups.find(group => group.id === fileGroupId)
      : this.fileGroups.find(group => group.fileType === 'PDF' && group.isOriginal);

    const group = preferredGroup ?? this.fileGroups.find(group => group.fileType === 'PDF');
    if (!group || !group.files?.length) {
      this.receiptFileId = '';
      this.selectedFileGroupId = '';
      return;
    }

    const pdfFile = group.files.find(file => file.path?.toLowerCase().endsWith('.pdf')) ?? group.files[0];
    this.receiptFileId = pdfFile?.id ?? '';
    this.selectedFileGroupId = group.id;
  }

  doEdit() {
    this.editMode = !this.editMode;
  }

  handleSelectedRevisionChange($event: string) {
    this.refreshRevisionDetails($event)
  }

  getRevisionDate(revisionId: string): Observable<RevisionDetails> {
    return this.revisionService.getRevisionDetails(revisionId);
  }

  // TODO dodać usuwanie ostatniej preferowanej rewizji przy ustawieniu nowej
  protected saveRevision() {
    this.revisionService.updateRevision(this.receiptDetails.preferredRevision!!).subscribe(
      {
        next: (revision: RevisionDetails) => {
          this.editMode = false;
          this.refreshRevisionDetails(revision.id);
        }
      }
    )
  }

  protected declineEdit() {
    this.refreshRevisionDetails(this.receiptDetails.preferredRevision?.id!!)
    this.editMode = false
  }

  protected refreshRevisions() {
    this.receiptService.getReceiptRevisions(this.receiptDetails.id)
      .subscribe({
        next: (revisions: Revision[]) => {
          this.receiptDetails.revisions = revisions;
        }
      })
  }

  protected refreshAliases() {
    this.revisionService.updateAliases(this.receiptDetails.preferredRevision?.id!!)
      .subscribe({})
  }

  protected refreshProductDict() {
    this.revisionService.updateProductDict(this.receiptDetails.preferredRevision?.id!!)
      .subscribe({})
  }

  private refreshRevisionDetails(revisionId: string) {
    if (revisionId) {
      this.getRevisionDate(revisionId).subscribe(
        {
          next: (revision) => {
            this.receiptDetails.preferredRevision = revision
          }
        }
      )
    } else {
      // @ts-ignore
      this.receiptDetails.preferredRevision = null;
    }
  }

  openAddFilesDialog(): void {
    const dialogRef = this.dialog.open(AddFilesDialogComponent, {
      width: '500px',
      disableClose: true,
      data: {receiptId: this.receiptDetails.id},
    });

    dialogRef.afterClosed().subscribe((uploaded?: boolean) => {
      if (uploaded) {
        this.refreshFileGroups();
      }
    });
  }

  private refreshFileGroups(): void {
    if (!this.receiptId) {
      return;
    }

    this.receiptService.getReceiptDetails(this.receiptId).subscribe({
      next: (res) => {
        this.fileGroups = res.fileGroups ?? [];
        this.selectPdfFile(this.selectedFileGroupId);
      }
    });
  }
}
