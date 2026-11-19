import {HttpErrorResponse} from '@angular/common/http';
import {isVersionConflict} from './version-conflict';

describe('isVersionConflict', () => {
  it('recognizes a 409 response', () => {
    expect(isVersionConflict(new HttpErrorResponse({status: 409}))).toBeTrue();
  });

  it('ignores other errors', () => {
    expect(isVersionConflict(new HttpErrorResponse({status: 400}))).toBeFalse();
    expect(isVersionConflict(new Error('boom'))).toBeFalse();
  });
});
