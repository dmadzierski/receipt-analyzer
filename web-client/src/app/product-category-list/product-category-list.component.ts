import {CommonModule} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatTableModule} from '@angular/material/table';
import {CreateProductCategoryRequest, ProductCategory, UpdateProductCategoryRequest} from '../model/product-category.model';
import {ProductCategoryService} from '../service/product-category.service';

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

    const request: UpdateProductCategoryRequest = {name};
    this.productCategoryService.updateProductCategory(category.id, request).subscribe(() => {
      this.editingId = null;
      this.loadCategories();
    });
  }

  deleteCategory(category: EditableProductCategory): void {
    this.productCategoryService.deleteProductCategory(category.id).subscribe(() => {
      this.loadCategories();
    });
  }
}
