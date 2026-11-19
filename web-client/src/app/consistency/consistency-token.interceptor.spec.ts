import {TestBed} from '@angular/core/testing';
import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {CONSISTENCY_TOKEN_HEADER, consistencyTokenInterceptor} from './consistency-token.interceptor';

describe('consistencyTokenInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([consistencyTokenInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('sends the token of the last command with the next reads and then forgets it', () => {
    http.put('/api/revisions/1', {}).subscribe();
    backend.expectOne('/api/revisions/1').flush({id: '1'}, {headers: {[CONSISTENCY_TOKEN_HEADER]: 'c1:2'}});

    http.get('/api/receipts/1').subscribe();
    http.get('/api/receipts/1/revisions').subscribe();
    const details = backend.expectOne('/api/receipts/1');
    const revisions = backend.expectOne('/api/receipts/1/revisions');
    expect(details.request.headers.get(CONSISTENCY_TOKEN_HEADER)).toBe('c1:2');
    expect(revisions.request.headers.get(CONSISTENCY_TOKEN_HEADER)).toBe('c1:2');
    details.flush({});
    revisions.flush([]);

    http.get('/api/wallets').subscribe();
    expect(backend.expectOne('/api/wallets').request.headers.has(CONSISTENCY_TOKEN_HEADER)).toBeFalse();
  });

  it('does not touch requests outside the api', () => {
    http.post('/api/wallets', {}).subscribe();
    backend.expectOne('/api/wallets').flush({id: '1'}, {headers: {[CONSISTENCY_TOKEN_HEADER]: 'c1:1'}});

    http.get('./i18n/en.json').subscribe();
    expect(backend.expectOne('./i18n/en.json').request.headers.has(CONSISTENCY_TOKEN_HEADER)).toBeFalse();
  });

  it('keeps no token when the command changed nothing', () => {
    http.post('/api/users/sync/me', {}).subscribe();
    backend.expectOne('/api/users/sync/me').flush({});

    http.get('/api/wallets').subscribe();
    expect(backend.expectOne('/api/wallets').request.headers.has(CONSISTENCY_TOKEN_HEADER)).toBeFalse();
  });
});
