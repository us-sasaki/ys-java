import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule as NgRouterModule } from '@angular/router';
import { UpgradeModule as NgUpgradeModule } from '@angular/upgrade/static';
import { CoreModule, RouterModule } from '@c8y/ngx-components';
import {
  DashboardUpgradeModule,
  UpgradeModule,
  HybridAppModule,
  UPGRADE_ROUTES
} from '@c8y/ngx-components/upgrade';
import { SubAssetsModule } from '@c8y/ngx-components/sub-assets';
import { ChildDevicesModule } from '@c8y/ngx-components/child-devices';
import {
  CockpitDashboardModule,
  ReportDashboardModule
} from '@c8y/ngx-components/context-dashboard';
import { ReportsModule } from '@c8y/ngx-components/reports';
import { SensorPhoneModule } from '@c8y/ngx-components/sensor-phone';
import { BinaryFileDownloadModule } from '@c8y/ngx-components/binary-file-download';
import { SearchModule } from '@c8y/ngx-components/search';
import { AssetsNavigatorModule } from '@c8y/ngx-components/assets-navigator';
import { CockpitConfigModule } from '@c8y/ngx-components/cockpit-config';

import { EmptyModule } from './custom-widgets/empty/empty.module';
import { ThingsCloudModule } from './custom-widgets/common/thingsCloud.module';

import { C8yService } from './custom-widgets/common/c8y/c8y.service';
import { C8yCommonService } from './custom-widgets/common/c8y/c8ycommon.service';

@NgModule({
  imports: [
    // Upgrade module must be the first
    UpgradeModule,
    BrowserAnimationsModule,
    RouterModule.forRoot(),
    NgRouterModule.forRoot([...UPGRADE_ROUTES], { enableTracing: false, useHash: true }),
    CoreModule.forRoot(),
    ReportsModule,
    NgUpgradeModule,
    AssetsNavigatorModule,
    DashboardUpgradeModule,
    CockpitDashboardModule,
    SensorPhoneModule,
    ReportDashboardModule,
    BinaryFileDownloadModule,
    SearchModule,
    SubAssetsModule,
    ChildDevicesModule,
    CockpitConfigModule,
    ThingsCloudModule,
    EmptyModule,
  ],
  declarations: [
  ],
  entryComponents: [
  ],
  providers: [
    C8yService,
    C8yCommonService
  ]
})
export class AppModule extends HybridAppModule {
  constructor(protected upgrade: NgUpgradeModule) {
    super();
  }
}
