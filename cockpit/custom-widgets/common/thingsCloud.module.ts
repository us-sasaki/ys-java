import { Injectable, NgModule } from '@angular/core';
import { FetchClient } from '@c8y/client';
import * as TCDS from '../../tc-client/src/thingsCloudDataSupplier';

@Injectable()
class ThingsCloudDataSupplier extends TCDS.ThingsCloudDataSupplier {
  constructor(fetchClient: FetchClient) {
    super(fetchClient);
  }
}

@NgModule({
  imports: [
  ],
  declarations: [
  ],
  entryComponents: [
  ],
  providers: [
    {
      provide: TCDS.ThingsCloudDataSupplier,
      useClass: ThingsCloudDataSupplier
    }
  ]
})
export class ThingsCloudModule { }
