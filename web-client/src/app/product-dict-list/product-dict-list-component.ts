import {CommonModule, NgClass} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList, DragDropModule} from '@angular/cdk/drag-drop';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {forkJoin} from 'rxjs';
import {
  Alias,
  GetProductDictListResponse,
  ProductDict,
  UpdateProductDictListRequest,
  UpdateProductDictListRequestItem
} from '../model/receipt-dict.model';
import {ProductDictService} from '../service/product-dict.service';
import {ProductCategory} from '../model/product-category.model';
import {ProductCategoryService} from '../service/product-category.service';

interface EditableProductDictGroup {
  primaryId: string;
  canonicalName: string;
  draftCategoryId: string | null;
  aliases: Alias[];
  mergedDicts: ProductDict[];
}

@Component({
  selector: 'app-product-dict-list',
  imports: [
    CommonModule,
    CdkDrag,
    CdkDragHandle,
    CdkDropList,
    DragDropModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    NgClass
  ],
  templateUrl: './product-dict-list-component.html',
  styleUrl: './product-dict-list-component.scss',
})
export class ProductDictListComponent implements OnInit {
  dataSource: EditableProductDictGroup[] = [];
  originalDataSource: EditableProductDictGroup[] = [];
  productCategories: ProductCategory[] = [];
  contentEditable = false;
  activeDragId: string | null = null;
  activeTargetId: string | null = null;

  constructor(
    private readonly productDictService: ProductDictService,
    private readonly productCategoryService: ProductCategoryService
  ) {
  }

  ngOnInit(): void {
    this.refreshView();
  }

  protected saveProductDict() {
    const changedItems = this.getChangedRequestItems();
    if (changedItems.length === 0) {
      this.contentEditable = false;
      this.clearDragState();
      return;
    }

    const request: UpdateProductDictListRequest = {
      items: changedItems
    };

    this.productDictService.updateProductDictList(request).subscribe(() => {
      this.contentEditable = false;
      this.clearDragState();
      this.refreshProductDictList();
    });
  }

  protected declineEdit() {
    this.refreshProductDictList();
    this.contentEditable = false;
    this.clearDragState();
  }

  protected doEdit() {
    this.contentEditable = true;
    this.originalDataSource = this.cloneGroups(this.dataSource);
  }

  protected onDragStarted(group: EditableProductDictGroup) {
    if (!this.contentEditable) {
      return;
    }

    this.activeDragId = group.primaryId;
    this.activeTargetId = group.primaryId;
  }

  protected onDragEnded() {
    this.clearDragState();
  }

  protected drop(event: CdkDragDrop<EditableProductDictGroup>, targetGroup: EditableProductDictGroup) {
    if (!this.contentEditable) {
      return;
    }

    const sourceGroup = event.item.data as EditableProductDictGroup;

    if (sourceGroup.primaryId === targetGroup.primaryId) {
      return;
    }

    const sourceIndex = this.dataSource.findIndex((group) => group.primaryId === sourceGroup.primaryId);
    const targetIndex = this.dataSource.findIndex((group) => group.primaryId === targetGroup.primaryId);

    if (sourceIndex < 0 || targetIndex < 0) {
      return;
    }

    const source = this.dataSource[sourceIndex];
    const target = this.dataSource[targetIndex];

    target.aliases = this.mergeAliases(target.aliases, this.collectAliases(source));
    target.mergedDicts = [...target.mergedDicts, this.toProductDict(source), ...source.mergedDicts];
    this.dataSource.splice(sourceIndex, 1);
  }

  protected onDropListEntered(group: EditableProductDictGroup) {
    if (this.contentEditable && this.activeDragId && this.activeDragId !== group.primaryId) {
      this.activeTargetId = group.primaryId;
    }
  }

  protected onDropListExited(group: EditableProductDictGroup) {
    if (this.activeTargetId === group.primaryId) {
      this.activeTargetId = null;
    }
  }

  protected canMerge = (drag: CdkDrag<EditableProductDictGroup>, drop: CdkDropList<EditableProductDictGroup>) => {
    if (!this.contentEditable) {
      return false;
    }

    return drag.data.primaryId !== drop.data.primaryId;
  };

  protected getCategoryName(categoryId: string | null): string {
    if (!categoryId) {
      return 'No category';
    }

    return this.productCategories.find((category) => category.id === categoryId)?.name || 'No category';
  }

  private refreshView() {
    forkJoin({
      productDicts: this.productDictService.getProductDictList(),
      productCategories: this.productCategoryService.getProductCategoryList()
    }).subscribe(({productDicts, productCategories}) => {
      this.productCategories = productCategories.items;
      this.dataSource = productDicts.items.map((item) => this.toGroup(item));
      this.originalDataSource = this.cloneGroups(this.dataSource);
    });
  }

  private refreshProductDictList() {
    this.productDictService
      .getProductDictList()
      .subscribe((result: GetProductDictListResponse) => {
        this.dataSource = result.items.map((item) => this.toGroup(item));
        this.originalDataSource = this.cloneGroups(this.dataSource);
      });
  }

  private toGroup(item: ProductDict): EditableProductDictGroup {
    return {
      primaryId: item.id,
      canonicalName: item.name,
      draftCategoryId: this.resolveCategoryId(item),
      aliases: item.aliases.map((alias) => ({...alias})),
      mergedDicts: []
    };
  }

  private toProductDict(group: EditableProductDictGroup): ProductDict {
    return {
      id: group.primaryId,
      name: group.canonicalName,
      productCategoryId: group.draftCategoryId,
      aliases: group.aliases.map((alias) => ({...alias}))
    };
  }

  private toRequestItem(group: EditableProductDictGroup): UpdateProductDictListRequestItem {
    return {
      canonicalName: group.canonicalName,
      productCategoryId: group.draftCategoryId,
      productDictList: [group.primaryId, ...group.mergedDicts.map((dict) => dict.id)]
    };
  }

  private collectAliases(group: EditableProductDictGroup): Alias[] {
    return [...group.aliases, ...group.mergedDicts.flatMap((dict) => dict.aliases)];
  }

  private mergeAliases(existing: Alias[], incoming: Alias[]): Alias[] {
    const aliases = new Map<string, Alias>();

    [...existing, ...incoming].forEach((alias) => {
      if (!aliases.has(alias.alias)) {
        aliases.set(alias.alias, {...alias});
      }
    });

    return [...aliases.values()];
  }

  private clearDragState() {
    this.activeDragId = null;
    this.activeTargetId = null;
  }

  private getChangedRequestItems(): UpdateProductDictListRequestItem[] {
    const originalByPrimaryId = new Map(
      this.originalDataSource
        .map((group) => this.toRequestItem(group))
        .map((item) => [item.productDictList[0], item] as const)
    );

    return this.dataSource
      .map((group) => this.toRequestItem(group))
      .filter((item) => this.hasRequestItemChanged(item, originalByPrimaryId.get(item.productDictList[0])));
  }

  private hasRequestItemChanged(
    current: UpdateProductDictListRequestItem,
    original?: UpdateProductDictListRequestItem
  ): boolean {
    if (!original) {
      return true;
    }

    if (current.canonicalName !== original.canonicalName) {
      return true;
    }

    if (current.productCategoryId !== original.productCategoryId) {
      return true;
    }

    if (current.productDictList.length !== original.productDictList.length) {
      return true;
    }

    return current.productDictList.some((id, index) => id !== original.productDictList[index]);
  }

  private cloneGroups(groups: EditableProductDictGroup[]): EditableProductDictGroup[] {
    return groups.map((group) => ({
      primaryId: group.primaryId,
      canonicalName: group.canonicalName,
      draftCategoryId: group.draftCategoryId,
      aliases: group.aliases.map((alias) => ({...alias})),
      mergedDicts: group.mergedDicts.map((dict) => ({
        id: dict.id,
        name: dict.name,
        productCategoryId: this.resolveCategoryId(dict),
        aliases: dict.aliases.map((alias) => ({...alias}))
      }))
    }));
  }

  private resolveCategoryId(item: ProductDict): string | null {
    return item.productCategory?.id ?? item.productCategoryId ?? null;
  }
}
