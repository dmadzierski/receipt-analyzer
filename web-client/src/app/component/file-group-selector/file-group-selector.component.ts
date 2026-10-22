import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FileGroup} from '../../model/receipt.model';

@Component({
  selector: 'app-file-group-selector',
  imports: [
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './file-group-selector.component.html',
  styleUrl: './file-group-selector.component.scss',
})
export class FileGroupSelectorComponent {
  @Input() fileGroups: FileGroup[] = [];
  @Input() selectedFileGroupId = '';
  @Output() fileGroupChange = new EventEmitter<string>();
  @Output() fileGroupDelete = new EventEmitter<string>();

  label(group: FileGroup): string {
    return `${group.fileType} (${group.isOriginal ? 'original' : 'copy'})`;
  }

  get selectedLabel(): string {
    const group = this.fileGroups.find(g => g.id === this.selectedFileGroupId);
    return group ? this.label(group) : '';
  }

  onSelectionChange(fileGroupId: string): void {
    this.fileGroupChange.emit(fileGroupId);
  }

  onDelete(event: Event, fileGroupId: string): void {
    event.stopPropagation();
    this.fileGroupDelete.emit(fileGroupId);
  }
}
