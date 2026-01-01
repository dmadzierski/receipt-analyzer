import {Component, OnInit} from '@angular/core';
import {ReceiptService} from '../service/receipt.service';
import {GetReceiptDetailsResponse} from '../model/receipt.model';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {provideNativeDateAdapter} from '@angular/material/core';
import {RevisionDetailsComponent} from '../component/revision-details/revision-details.component';

@Component({
  selector: 'app-receipt-details',
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    MatDatepickerModule,
    MatButtonModule,
    RevisionDetailsComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './receipt-details.component.html',
  styleUrl: './receipt-details.component.scss',
})
export class ReceiptDetailsComponent implements OnInit {
  constructor(
    private receiptService: ReceiptService,
    private readonly router: Router,
    private route: ActivatedRoute
  ) {
  }

  receiptDetails = {} as GetReceiptDetailsResponse;

  ngOnInit(): void {
    const receiptId = this.route.snapshot.paramMap.get('id');
    if (receiptId) {
      this.receiptService.getReceiptDetails(receiptId).subscribe((res) => {
        this.receiptDetails = res;
        console.log(this.receiptDetails);
      }, error => this.router.navigate(["/"]));
    } else {
    }
  }
}
