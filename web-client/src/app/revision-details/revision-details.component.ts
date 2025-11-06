import { Component, OnInit } from '@angular/core';
import { ReceiptService } from '../service/receipt.service';
import { GetReceiptDetailsResponse } from '../model/receipt.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-revision-details',
  imports: [],
  templateUrl: './revision-details.component.html',
  styleUrl: './revision-details.component.scss',
})
export class RevisionDetails implements OnInit {
  constructor(
    private receiptService: ReceiptService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {}
}
