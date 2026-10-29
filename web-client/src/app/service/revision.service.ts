import {formatDate} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {RevisionDetails,} from '../model/receipt.model';
import {CreateRevisionCopy} from '../model/revision.model';

@Injectable({
  providedIn: 'root',
})
export class RevisionService {
  private readonly httpClient = inject(HttpClient);


  copyRevision(revisionCopy: string): Observable<CreateRevisionCopy> {
    return this.httpClient.post<CreateRevisionCopy>(`/api/revisions/${revisionCopy}/copy`, {});
  }

  getRevisionDetails(revisionId: string) {
    return this.httpClient.get<RevisionDetails>(`/api/revisions/${revisionId}`);
  }

  updateRevision(revision: RevisionDetails) {
    const paymentDate: unknown = revision.paymentDate;
    const body = paymentDate instanceof Date
      ? {...revision, paymentDate: formatDate(paymentDate, "yyyy-MM-dd'T'HH:mm:ss", 'en-US')}
      : revision;
    return this.httpClient.put<RevisionDetails>(`/api/revisions/${revision.id}`, body)
  }

  deleteRevision(id: string) {
    return this.httpClient.delete<void>(`/api/revisions/${id}`);
  }

  updateAliases(id: string) {
    return this.httpClient.post<null>(`/api/revisions/${id}/aliases`, {})
  }

  rewriteProductNames(id: string) {
    return this.httpClient.post<null>(`/api/revisions/${id}/rewrite-product-names`, {})
  }

  updateProductDict(id: string) {
    return this.httpClient.post<null>(`/api/revisions/${id}/update-product-dict`, {})
  }
}
