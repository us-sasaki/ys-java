import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EmptyType } from './empty.type';

import { C8yService } from "../common/c8y/c8y.service";
import { ThingsCloudDataSupplier } from '../../tc-client/src/thingsCloudDataSupplier';

@Injectable()
export class EmptyService {

  constructor(
    private c8yService: C8yService,
    private tcd: ThingsCloudDataSupplier
  ) {
  }

  async f(config: any) {
    console.log("f:config", config);
    console.log("devices", await this.tcd.getDevicesArray(config.device.id));
  }

}
