import {CommonModule} from '@angular/common';
import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogComponent, ConfirmDialogData} from '../component/confirm-dialog/confirm-dialog.component';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatTableModule} from '@angular/material/table';
import {
  CreateProductCategoryRequest,
  ProductCategory,
  UpdateProductCategoryRequest
} from '../model/product-category.model';
import {ProductCategoryService} from '../service/product-category.service';
import {isVersionConflict} from '../error/version-conflict';

interface EditableProductCategory extends ProductCategory {
  draftName: string;
}

@Component({
  selector: 'app-product-category-list',
  templateUrl: './product-category-list.component.html',
  styleUrl: './product-category-list.component.scss',
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatTableModule],
})
export class ProductCategoryListComponent implements OnInit {
  dataSource: EditableProductCategory[] = [];
  newCategoryName = '';
  editingId: string | null = null;
  displayedColumns: string[] = ['name', 'actions'];

  private readonly dialog = inject(MatDialog);

  constructor(private readonly productCategoryService: ProductCategoryService) {
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.productCategoryService.getProductCategoryList().subscribe((result) => {
      this.dataSource = result.items.map((item) => ({...item, draftName: item.name}));
      this.editingId = null;
    });
  }

  addCategory(): void {
    const name = this.newCategoryName.trim();
    if (!name) {
      return;
    }

    const request: CreateProductCategoryRequest = {name};
    this.productCategoryService.createProductCategory(request).subscribe(() => {
      this.newCategoryName = '';
      this.loadCategories();
    });
  }

  startEdit(category: EditableProductCategory): void {
    this.editingId = category.id;
    category.draftName = category.name;
  }

  cancelEdit(): void {
    this.editingId = null;
    this.loadCategories();
  }

  saveCategory(category: EditableProductCategory): void {
    const name = category.draftName.trim();
    if (!name) {
      return;
    }

    const request: UpdateProductCategoryRequest = {version: category.version, name};
    this.productCategoryService.updateProductCategory(category.id, request).subscribe({
      next: () => {
        this.editingId = null;
        this.loadCategories();
      },
      error: (error: unknown) => {
        if (isVersionConflict(error)) {
          this.editingId = null;
          this.loadCategories();
        }
      }
    });
  }

  deleteCategory(category: EditableProductCategory): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete category',
        message: `Delete category "${category.name}"?`,
        confirmLabel: 'Delete',
      } as ConfirmDialogData,
    }).afterClosed().subscribe((confirmed?: boolean) => {
      if (!confirmed) return;
      this.productCategoryService.deleteProductCategory(category.id).subscribe(() => {
        this.loadCategories();
      });
    });
  }
}
