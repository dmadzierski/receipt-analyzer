import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {FileGroup} from '../../model/receipt.model';

@Component({
  selector: 'app-file-group-selector',
  imports: [
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './file-group-selector.component.html',
  styleUrl: './file-group-selector.component.scss',
})
export class FileGroupSelectorComponent {
  @Input() fileGroups: FileGroup[] = [];
  @Input() selectedFileGroupId = '';
  @Output() fileGroupChange = new EventEmitter<string>();

  onSelectionChange(fileGroupId: string): void {
    this.fileGroupChange.emit(fileGroupId);
  }
}
