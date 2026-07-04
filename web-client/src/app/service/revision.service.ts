import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CreateReceiptResponse, RevisionDetails,} from '../model/receipt.model';
import {CreateRevisionCopy} from '../model/revision.model';

@Injectable({
  providedIn: 'root',
})
export class RevisionService {
  private readonly httpClient = inject(HttpClient);


  copyRevision(revisionCopy: string): Observable<CreateRevisionCopy> {
    return this.httpClient.post<CreateReceiptResponse>(`/api/revisions/${revisionCopy}/copy`, {});
  }

  getRevisionDetails(revisionId: string) {
    return this.httpClient.get<RevisionDetails>(`/api/revisions/${revisionId}`);
  }

  updateRevision(revision: RevisionDetails) {
    return this.httpClient.put<RevisionDetails>(`/api/revisions/${revision.id}`, revision)
  }
}
