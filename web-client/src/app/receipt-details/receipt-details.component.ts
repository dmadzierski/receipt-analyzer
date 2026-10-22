import {Component, OnInit} from '@angular/core';
import {ReceiptService} from '../service/receipt.service';
import {
  CreateRevisionRequest,
  FileGroup,
  FileType,
  GetReceiptDetailsResponse,
  ResolverStrategy,
  Revision,
  RevisionDetails
} from '../model/receipt.model';
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
import {FileGroupService} from '../service/file-group.service';
import {Observable} from 'rxjs';
import {FileGroupSelectorComponent} from '../component/file-group-selector/file-group-selector.component';
import {FilePreviewComponent} from '../component/file-preview/file-preview.component';
import {AddFilesDialogComponent} from '../component/add-files-dialog/add-files-dialog.component';
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from '../component/confirm-dialog/confirm-dialog.component';


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
    FileGroupSelectorComponent,
    FilePreviewComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './receipt-details.component.html',
  styleUrl: './receipt-details.component.scss',
})
export class ReceiptDetailsComponent implements OnInit {
  strategies = Object.keys(ResolverStrategy).filter(key => Number.isNaN(Number(key)));
  receiptDetails = {} as GetReceiptDetailsResponse;
  receiptFileId: string = '';
  previewFileIds: string[] = [];
  protected readonly FileType = FileType;
  previewType: FileType | null = null;
  fileGroups: FileGroup[] = [];
  selectedFileGroupId: string = '';
  editMode: boolean = false;
  receiptId: string = '';
  createRevisionData: CreateRevisionRequest = {} as CreateRevisionRequest;

  constructor(
    private readonly receiptService: ReceiptService,
    private readonly revisionService: RevisionService,
    private readonly fileGroupService: FileGroupService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.receiptId = this.route.snapshot.paramMap.get('id') ?? '';
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
    const group = (fileGroupId ? this.fileGroups.find(g => g.id === fileGroupId) : undefined)
      ?? this.fileGroups.find(g => g.fileType === FileType.PDF && g.isOriginal)
      ?? this.fileGroups.find(g => g.isOriginal)
      ?? this.fileGroups.find(g => g.fileType === FileType.PDF)
      ?? this.fileGroups[0];

    this.receiptFileId = '';
    this.previewFileIds = [];
    this.previewType = null;
    this.selectedFileGroupId = '';

    if (!group || !group.files?.length) {
      return;
    }
    this.selectedFileGroupId = group.id;

    const files = [...group.files];
    if (group.fileType === FileType.PDF) {
      const pdfFile = files.find(file => file.path?.toLowerCase().endsWith('.pdf')) ?? files[0];
      this.receiptFileId = pdfFile?.id ?? '';
      this.previewType = FileType.PDF;
    } else if (group.fileType === FileType.JSON) {
      this.previewFileIds = files.map(file => file.id);
      this.previewType = FileType.JSON;
    } else {
      this.previewFileIds = files.map(file => file.id);
      this.previewType = group.fileType as FileType;
    }
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

  // TODO dodaÄ‡ usuwanie ostatniej preferowanej rewizji przy ustawieniu nowej
  protected saveRevision() {
    this.revisionService.updateRevision(this.receiptDetails.preferredRevision!!).subscribe(
      {
        next: (revision: RevisionDetails) => {
          this.editMode = false;
          this.refreshRevisionDetails(revision.id);
          this.refreshRevisions();
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

  protected rewriteProductNames() {
    this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Rewrite product names',
        message: 'Rewrite product names for this revision?',
        confirmLabel: 'Rewrite',
      } as ConfirmDialogData,
    }).afterClosed().subscribe((confirmed?: boolean) => {
      if (confirmed) {
        this.revisionService.rewriteProductNames(this.receiptDetails.preferredRevision?.id!!)
          .subscribe({})
      }
    });
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

  deleteFileGroup(fileGroupId: string): void {
    if (!fileGroupId) return;
    this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete file group',
        message: 'Delete this file group?',
        confirmLabel: 'Delete',
      } as ConfirmDialogData,
    }).afterClosed().subscribe((confirmed?: boolean) => {
      if (!confirmed) return;
      this.fileGroupService.deleteFileGroup(fileGroupId).subscribe({
        next: () => this.refreshFileGroups(),
        error: (err) => console.error(err)
      });
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

  createRevision(): void {
    this.createRevisionData.fileGroupId = this.selectedFileGroupId
    if (this.createRevisionData.fileGroupId == null || this.createRevisionData.strategy == null) {
      return;
    }

    this.receiptService.createRevision(this.receiptId, this.createRevisionData).subscribe({
      next: () => this.refreshRevisions()
    })
  }

}

