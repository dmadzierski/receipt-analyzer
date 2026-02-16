import {Component, OnInit} from '@angular/core';
import {ReceiptService} from '../service/receipt.service';
import {GetReceiptDetailsResponse, Revision, RevisionDetails} from '../model/receipt.model';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {provideNativeDateAdapter} from '@angular/material/core';
import {RevisionDetailsComponent} from '../component/revision-details/revision-details.component';
import {PdfViewerComponent} from '../component/pdf-viewer/pdf-viewer.component';
import {RevisionListComponent} from '../component/revision-list/revision-list.component';
import {MatIcon} from '@angular/material/icon';
import {RevisionService} from '../service/revision.service';
import {Observable} from 'rxjs';

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
    MatIcon
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './receipt-details.component.html',
  styleUrl: './receipt-details.component.scss',
})
export class ReceiptDetailsComponent implements OnInit {
  constructor(
    private receiptService: ReceiptService,
    private revisionService: RevisionService,
    private readonly router: Router,
    private route: ActivatedRoute
  ) {
  }

  receiptDetails = {} as GetReceiptDetailsResponse;

  receiptFileId: string = '';

  editRevisionMode: boolean = false;

  ngOnInit(): void {
    const receiptId = this.route.snapshot.paramMap.get('id');
    if (receiptId) {
      this.receiptService.getReceiptDetails(receiptId).subscribe((res) => {
        this.receiptDetails = res;
        this.receiptFileId = this.receiptDetails.preferredRevision.files.find(k => k.rawData === null)?.id ?? '';
      }, () => this.router.navigate(["/"]));
    } else {
    }
  }

  doEdit() {
    this.editRevisionMode = !this.editRevisionMode;
  }

  handleSelectedRevisionChange($event: string) {
    this.refreshRevisionDetails($event)
  }

  getRevisionDate(revisionId: string): Observable<RevisionDetails> {
    return this.revisionService.getRevisionDetails(revisionId);
  }

  protected saveRevision() {
    this.revisionService.updateRevision(this.receiptDetails.preferredRevision).subscribe(
      {
        next: (revision: RevisionDetails) => {
          this.receiptDetails.preferredRevision = revision;
          this.editRevisionMode = false;
          this.refreshRevision(this.receiptDetails.id);
        }
      }
    )
  }

  protected declineEdit() {
    this.refreshRevisionDetails(this.receiptDetails.preferredRevision.id)
    this.editRevisionMode = false
  }

  private refreshRevisionDetails(revisionId: string) {
    this.getRevisionDate(revisionId).subscribe(
      {
        next: (revision) => {
          this.receiptDetails.preferredRevision = revision
        }
      }
    )
  }

  protected refreshRevision(id: any) {
    this.receiptService.getReceiptRevisions(this.receiptDetails.id)
      .subscribe({
        next: (revisions: Revision[]) => {
          this.receiptDetails.revisions = revisions;
        }
      })
  }
}
