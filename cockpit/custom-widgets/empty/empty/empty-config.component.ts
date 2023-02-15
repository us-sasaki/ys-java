import { Component, Input } from '@angular/core';

@Component({
  selector: 'empty-config',
  templateUrl: './empty-config.component.html'
})
export class EmptyConfig {
  @Input() config: any = {};
}
