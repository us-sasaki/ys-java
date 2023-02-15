import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HOOK_COMPONENTS } from '@c8y/ngx-components';

import { Empty } from './empty/empty.component';
import { EmptyConfig } from './empty/empty-config.component';

import { EmptyService } from './empty.service';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        Empty,
        EmptyConfig,
    ],
    entryComponents: [
        Empty,
        EmptyConfig,
    ],
    providers: [
        EmptyService,
        {
            provide: HOOK_COMPONENTS,
            multi: true,
            useValue: {
                id: 'empty.widget',
                label: 'Empty Widget',
                previewImage: require('../../empty-widget-preview.png'),
                description: 'An Empty Widget.',
                component: Empty,
                configComponent: EmptyConfig,
                data: {
                    ng1: {
                        options: {
                            groupsSelectable: true,
                        },
                    },
                },
            },
        },
    ],
    schemas: [
      CUSTOM_ELEMENTS_SCHEMA
    ]
})

export class EmptyModule {}
