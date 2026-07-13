import {Component, ElementRef, inject, Input, ViewChild,} from "@angular/core";
import * as pdfjsLib from "pdfjs-dist";
import Keycloak from 'keycloak-js';

@Component({
  selector: "app-pdf-viewer",
  templateUrl: "./pdf-viewer.component.html",
  styleUrls: ["./pdf-viewer.component.scss"],
})
export class PdfViewerComponent {

  keycloak = inject(Keycloak);

  @ViewChild("pdfContainer", {static: true})
  pdfContainer!: ElementRef<HTMLDivElement>;
  private pdfDocument: any;
  private currentPageNumber = 1;
  private scale = 1.5;
  totalPages = 0;
  currentPage = 1;

  private _receiptFileId: string = '';

  @Input()
  set receiptFileId(value: string) {
    this._receiptFileId = value;
    if (value && value.trim() !== '') {
      this.loadPdf();
    }
  }

  async loadPdf() {
    try {
      const pdfjs = pdfjsLib as any;
      pdfjs.GlobalWorkerOptions.workerSrc = "assets/pdf.worker.min.mjs";
      const loadingTask = pdfjsLib.getDocument({
        url: `http://localhost:4200/api/receipt-files/${this._receiptFileId}`,
        httpHeaders: {
          'Authorization': `Bearer ${this.keycloak.token}`
        },
        withCredentials: true
      });
      this.pdfDocument = await loadingTask.promise;
      this.totalPages = this.pdfDocument.numPages;
      this.renderPage(this.currentPageNumber);
    } catch (error) {
      console.error(error);
    }
  }

  async renderPage(pageNumber: number) {
    const page = await this.pdfDocument.getPage(pageNumber);
    const viewport = page.getViewport({scale: this.scale});

    const container = this.pdfContainer.nativeElement;
    container.innerHTML = "";

    const canvas = document.createElement("canvas");
    container.appendChild(canvas);

    const context = canvas.getContext("2d")!;
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    const renderContext = {
      canvasContext: context,
      viewport: viewport,
    };

    await page.render(renderContext).promise;
  }

  goToPrevPage() {
    if (this.currentPageNumber > 1) {
      this.currentPageNumber--;
      this.currentPage = this.currentPageNumber;
      this.renderPage(this.currentPageNumber);
    }
  }

  goToNextPage() {
    if (this.currentPageNumber < this.totalPages) {
      this.currentPageNumber++;
      this.currentPage = this.currentPageNumber;
      this.renderPage(this.currentPageNumber);
    }
  }

  zoomIn() {
    this.scale += 0.25;
    this.renderPage(this.currentPageNumber);
  }

  zoomOut() {
    if (this.scale > 0.5) {
      this.scale -= 0.25;
      this.renderPage(this.currentPageNumber);
    }
  }
}
