import { 
  AlarmService,
  EventService,
  IAlarm,
  IEvent,
  IFetchResponse,
  IIdentified,
  IManagedObject, 
  IManagedObjectBinary,
  IMeasurement, 
  InventoryService,
  InventoryBinaryService,
  IResult,
  IResultList,
  MeasurementService,
  FetchClient,
  Realtime,
  Severity,
  AlarmStatus,
  IFetchOptions
 } from "@c8y/client";
import { deviceAvailabilityIconMap, resolveInjectedFactories } from "@c8y/ngx-components";

export class ThingsCloudClient {
  private measurementService: MeasurementService;
  private alarmService: AlarmService;
  private eventService: EventService;
  private inventoryService: InventoryService;
  private inventoryBinaryService: InventoryBinaryService;
  private realtime: Realtime;
  
  /**
   * Things Cloudのデータを取得する
   * @class 
   * @param {FetchClient} deviceId deviceId or groupId
   * 
   * 
   */
  constructor(
    private fetchClient: FetchClient
  ) {
    this.measurementService = new MeasurementService(fetchClient);
    this.alarmService = new AlarmService(fetchClient);
    this.eventService = new EventService(fetchClient);
    this.inventoryService = new InventoryService(fetchClient);
    this.inventoryBinaryService = new InventoryBinaryService(fetchClient);
    this.realtime = new Realtime(fetchClient);
  }

  /**
   * Things Cloud に接続し、Alarm 一覧を取得する
   * 
   * @param {object} param 
   * 
   * @returns Promise<IResultList<IAlarm>>
   * 
   * @example
   * ```typescript
   *    
   *     const alarmsFilter = {
   *        deviceId: "123456",
   *        filter: {
   *          pageSize: 10,
   *          type: "c8y_UnavailabilityAlarm"
   *        }
   *     };
   * 
   *    (async () => {
   *       const alarms = await thingsCloudClient.getAlarms(alarmsFilter);
   *    })();
   * ```
   */
  async getAlarms(param: {deviceId: string, filter?: any}): Promise<IResultList<IAlarm>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    requestFilter.withTotalPages = true;

    return await this.alarmService.list(requestFilter);
  }
  
  /**
   * Things Cloud に接続し、Event 一覧を取得する
   * 
   * @param {object} param 
   * 
   * @returns Promise<IResultList<IEvent>>
   * 
   * @example
   * ```typescript
   *    
   *     const eventsFilter = {
   *        deviceId: "123456",
   *        filter: {
   *          pageSize: 10,
   *          type: "c8y_LocationUpdate"
   *        }
   *     };
   * 
   *    (async () => {
   *       const events = await thingsCloudClient.getEvents(eventsFilter);
   *    })();
   * ```
   */
  async getEvents(param: {deviceId: string, filter?: any}): Promise<IResultList<IEvent>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    requestFilter.withTotalPages = true;

    return await this.eventService.list(requestFilter);
  }

  /**
   * Things Cloud に接続し、Measurement 一覧を取得する
   * 
   * @param {object} param 
   * 
   * @returns Promise<IResultList<IMeasurement>>
   * 
   * @example
   * ```typescript
   *    
   *     const measurementsFilter = {
   *        deviceId: "123456",
   *        filter: {
   *          pageSize: 10,
   *          valueFragmentType: "c8y_Temperature"
   *        }
   *     };
   * 
   *    (async () => {
   *       const measurements = await thingsCloudClient.getMeasurements(measurementsFilter);
   *    })();
   * ```
   */
  async getMeasurements(param: {deviceId: string, filter?: any}): Promise<IResultList<IMeasurement>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    return await this.measurementService.list(requestFilter);
  }

  /**
   * ManaggedObject を取得する
   *
   * @param {object} filter ManaggedObject filter情報
   *
   * @returns 指定したFilterで取得した ManaggedObject 情報
   * 
   * @example
   * ```typescript
   *  const filter: object = {
   *     pageSize: 100,
   *     withTotalPages: true
   *   };
   * 
   *    (async () => {
   *       const managedObjects = await thingsCloudClient.getManagedObjects(filter);
   *    })();
   * ```
   */
  async getManagedObjects(filter?: object): Promise<IManagedObject[]> {
    return (await this.inventoryService.list(filter)).data;
  }
  
  /**
   * MO ID に対し、サブアセット（ManagedObject の ID）を取得する。
   * ID がデバイスの場合、単独のデバイスが設定され、アセットの場合、再帰的に
   * 子アセットのデバイスが設定される。
   * 
   * @param {string} moId MO ID
   * 
   * @returns Promise デバイスIDの配列
   * 
   * @example
   * ```typescript
   *    
   *     const moId = "123456";
   * 
   *    (async () => {
   *       const moIds = await thingsCloudClient.getSubAssets(moId);
   *    })();
   * ```
   */
  async getSubAssets(moId: string): Promise<string[]> {
    const mos = await this.getSubAssetMOs(moId);
    return mos.map( mo => mo.id );
  }

  /**
   * MO ID に対し、サブアセット（ManagedObject）を取得する。
   * ID がデバイスの場合、単独のデバイスが設定され、アセットの場合、再帰的に
   * 子アセットのデバイスが設定される。
   * 
   * @param {string} moId MO ID
   * 
   * @returns Promise デバイス ManagedObject の配列
   * 
   * @example
   * ```typescript
   *    
   *     const moId = "123456";
   * 
   *    (async () => {
   *       const mos = await thingsCloudClient.getSubAssetMOs(moId);
   *    })();
   * ```
   */
  async getSubAssetMOs(moId: string): Promise<any[]> {
    const devices = [];
    await this.getSubAssetMOsImpl(moId, devices);
    
    return devices;
  }

  /**
   * リアルタイムにイベント等を取得するための Realtime オブジェクトを取得する
   *
   * @returns Realtime のインスタンス
   */
  getRealtime(): Realtime {
    return this.realtime;
  }

  /**
   * リアルタイム通知のチャネル監視を解除する
   * 
   * @param subscription チャネル監視中のサブスクリプション
   */
  unsubscribeRealtime(subscription: any): void {
    this.realtime.unsubscribe(subscription);
  }

  /**
   * デバイスかグループを判断し、デバイスの場合はdevicesへpush、
   * グループの場合は再帰的に関数を呼び出す。
   * 
   * @param {string} moId moId
   * @param {IManagedObject} devices デバイス情報のリスト
   * 
   * @returns Promise<void>
   */
  private async getSubAssetMOsImpl(moId: string, devices: IManagedObject[]): Promise<void> {
    const res = await this.inventoryService.detail(moId);
  
    if (res.data.childAssets.references.length > 0) {
      // 指定されたのはグループ
      for (const data of res.data.childAssets.references) {
        await this.getSubAssetMOsImpl(data.managedObject.id, devices); // 再帰
      }
    } else {
      // 指定されたのはデバイス
      if (devices.map( mo => mo.id).indexOf(res.data.id) > -1) return;
      devices.push(res.data);
    }
  }
  /**
   * ID をキーに ManaggedObject を取得する
   *
   * @param id ManaggedObject ID
   *
   * @returns 指定IDの ManagedObject 詳細情報
   */
  async getManagedObjectById(id: string): Promise<IManagedObject> {
    return (await this.inventoryService.detail(id)).data;
  }

  /**
   * 指定した fragmentType を持つ ManagedObject を取得します
   * @param fragmentType 保有する fragment type
   * @returns 指定 fragmentType を保有する ManagedObject
   */
  async getManagedObjectsByFragmentType(fragmentType: string): Promise<IManagedObject[]> {
    // const query = {
    //   __filter: {fragmentType: fragmentType}
    // };
    const filter = {
      fragmentType: fragmentType //this.inventoryService.queriesUtil.buildQuery(query)
    };
    return (await this.inventoryService.list(filter)).data;
  }

  /**
   * 指定された ManagedObject および子、孫、、の ManagedObject を取得し、返却します。
   * @param parentAssetId 親アセット(またはデバイス)ID
   * @returns 自分自身、および子アセット/子デバイスからなる ManagedObject 配列
   */
  async getAllSubAssets(parentAssetId: string): Promise<any[]> {
    const assets = [];
    await this.getAllSubAssetsImpl(parentAssetId, assets);
    return assets;
  }

  private async getAllSubAssetsImpl(parentAssetId: string, assets: any[]): Promise<void> {
    const parentAsset: IManagedObject = await this.getManagedObjectById(parentAssetId);
    if (assets.map( (asset: IManagedObject) => asset.id).indexOf(parentAsset.id) == -1)
      assets.push(parentAsset);
    if (parentAsset.childAssets.references.length > 0) {
      for (const child of parentAsset.childAssets.references) {
        await this.getAllSubAssetsImpl(child.managedObject.id, assets);
      }
      // const promises = parentAsset.childAssets.references.map( async (child) => 
      //   await this.getAllSubAssetsImpl(child.managedObject.id, assets) );
      // Promise.all(promises);
    }
  }

  /**
   * 指定されたマネージドオブジェクトを Things Cloud に登録する。
   * デバイスを登録したい場合、c8y_IsDevice を付与する必要があることに注意。
   * 
   * @param managedObject 登録する ManagedObject
   * @returns 変更後の ManagedObject
   */
  async postManagedObject(managedObject: Partial<IManagedObject>): Promise<IManagedObject> {
    const mo = (await this.inventoryService.create(managedObject)).data;
    return mo;
  }

  /**
   * 指定されたマネージドオブジェクトを Things Cloud から削除する。
   * @param id 削除する ManagedObject id
   */
  async deleteManagedObject(id: string): Promise<void> {
    await this.inventoryService.delete(id);
  }

  async putChildAsset(child: string, parent: string): Promise<IResult<IIdentified>> {
    return await this.inventoryService.childAssetsAdd(child, parent); // 結果(親 MO?) を捨てている
  }

  /**
   * InventoryService.childDevicesAdd 呼び出し
   * @param child 子デバイス id
   * @param parent 親デバイス id
   * 
   * @returns 子デバイス情報
   */
  async putChildDevice(child: string, parent: string): Promise<IResult<IIdentified>> {
    return await this.inventoryService.childDevicesAdd(child, parent);
  }

  /**
   * 指定されたメジャーメントを Things Cloud に登録する。
   * @param deviceId source として設定するデバイスID
   * @param type type
   * @param fragments メジャーメント値を格納するフラグメント、およびカスタムフラグメント
   * @param time 報告時刻
   * @returns 登録されたメジャーメント
   */
  async postMeasurement(deviceId: string, type: string, fragments?: any, time?: Date): Promise<IMeasurement> {
    const measurement: Partial<IMeasurement> = {
      type,
      source: { id: deviceId },
      time: time || new Date(),
      ...fragments
    };
    return (await this.measurementService.create(measurement)).data;
  }

  /**
   * 指定されたイベントを Things Cloud に登録する。
   * @param deviceId source として設定するデバイスID
   * @param type イベント type
   * @param text イベントの本文
   * @param time イベント発生時刻、省略時は現在時刻
   * @param fragments カスタムフラグメント
   * @returns 登録されたイベント
   */
  async postEvent(deviceId: string, type: string, text: string, time?: Date, fragments?: any): Promise<IEvent> {
    const event: IEvent = {
      type,
      source: { id: deviceId },
      time: time || new Date(),
      text,
      ...fragments
    };
    return (await this.eventService.create(event)).data;
  }

  /**
   * 指定されたアラームを Things Cloud に登録する。
   * @param deviceId source として設定するデバイスID
   * @param type アラーム type
   * @param text アラームの本文
   * @param severity アラームの重大度 Severity.CRITICAL, Severity.MAJOR, Severity.MINOR, Severity.WARNING のいずれか
   * @param status アラームのステータス　AlarmStatus.ACTIVE, AlarmStatus.ACKNOWLEDGED, AlarmStatus.CLEARED のいずれか
   * @param time アラーム発生時刻。省略時は現在時刻。
   * @param fragments カスタムフラグメント(重複除外となるケースでは無視されることに注意)
   * @returns 
   */
  async postAlarm(deviceId: string, type: string, text: string, severity: Severity, status: AlarmStatus, time?: Date, fragments?: any): Promise<IAlarm> {
    const alarm: IAlarm = {
      type,
      source: { id: deviceId },
      time: time || new Date(),
      text,
      severity,
      status,
      ...fragments
    };
    return (await this.alarmService.create(alarm)).data;
  }

  /**
   * 指定された内容でアラームを更新する。
   * @param alarmId 更新対象アラームのID
   * @param updater 更新内容
   * @returns 更新後のアラーム
   */
  async putAlarm(alarmId: string, updater: any): Promise<IAlarm> {
    const entity: Partial<IAlarm> = {
      id: alarmId,
      ...updater
    };
    return (await this.alarmService.update(entity)).data;
  }

  /**
   * 指定されたステータスにアラームを更新する。
   * @param alarmId 更新対象アラームのID
   * @param status 更新後の status 値
   * @returns 更新後のアラーム
   */
  async putAlarmStatus(alarmId: string, status: AlarmStatus): Promise<IAlarm> {
    return await this.putAlarm(alarmId, {status});
  }

  /**
   * 指定された重大度にアラームを更新する。
   * @param alarmId 更新対象アラームのID
   * @param severity 更新後の severity 値
   * @returns 更新後のアラーム
   */
   async putAlarmSeverity(alarmId: string, severity: Severity): Promise<IAlarm> {
    return await this.putAlarm(alarmId, {severity});
  }

  async getEPLFileList(): Promise<IEPLFile[]> {
    const options: IFetchOptions = {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    };
    const response = await this.fetchClient.fetch('/service/cep/eplfiles/', options);
    const json = await response.json();
    if (response.status === 200)
      return json.eplfiles as IEPLFile[];
    throw json;
  }

  async getEPLFile(id: string): Promise<IEPLFile> {
    const options: IFetchOptions = {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    };
    const response = await this.fetchClient.fetch(`/service/cep/eplfiles/${id}/`, options);
    const json = await response.json();
    if (response.status === 200) 
      return json as IEPLFile;
    throw json;
  }

  async postEPLFile(eplFile: Partial<IEPLFile>): Promise<IEPLFile> {
    const options: IFetchOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(eplFile)
    };
    const response = await this.fetchClient.fetch('/service/cep/eplfiles/', options);
    const json = await response.json();
    if (response.status === 201)
      return json as IEPLFile;
    throw json;
  }

  async putEPLFile(id: string, eplFile: Partial<IEPLFile>): Promise<IEPLFile> {
    const options: IFetchOptions = {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(eplFile)
    };
    const response = await this.fetchClient.fetch(`/service/cep/eplfiles/${id}`, options);
    const json = await response.json();
    if (response.status === 200)
      return json as IEPLFile;
    throw json;
  }

  async deleteEPLFile(id: string): Promise<void> {
    const options: IFetchOptions = {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json'},
    };
    const response = await this.fetchClient.fetch(`/service/cep/eplfiles/${id}`, options);
    if (response.status === 200) return;
    throw await response.json();
  }

  /**
   * 指定されたデバイスから報告された最後の Measurement を取得する。
   * 最近の Measurement の Promise が返却される。
   *
   * @param deviceId デバイスID
   * @param fragmentType measurement の fragmentType
   *
   * @returns 最近の IMeasurement の Promise
   */
  getLastMeasurement(
    deviceId: string,
    fragmentType: string
  ): Promise<IMeasurement> {
    const filter: any = {
      type: fragmentType,
      revert: true,
      dateFrom: "2000-01-01",
      source: deviceId,
      pageSize: 1
    };

    return this.measurementService.list(filter).then(resultList => resultList.data[0]);
  }

  /**
   * 指定 ID の InventoryBinary を取得する
   *
   * @param binaryId InventoryBinary ID
   *
   * @returns IfetchResponse の Promise
   */
  getBinary(binaryId: number | string): Promise<IFetchResponse>{
    return this.inventoryBinaryService.download(binaryId);
  }

  /**
   * 指定された Binary データを inventory の binary として保存します
   *
   * @param binaryData バイナリデータ(画像など)
   * @param managedObject ファイルに関するメタデータを含むオブジェクト
   *
   * @returns 保存後の IManagaedObjectBinary
   */
  createBinary(binaryData: Buffer | File | Blob, managedObject?: Partial<IManagedObject>): Promise<IResult<IManagedObjectBinary>> {
    return this.inventoryBinaryService.create(binaryData, managedObject);
  }
}

export interface IEPLFile {
  contents?: string,
  description: string,
  eplPackageName: string,
  errors: any[],
  id: string,
  name: string,
  state: string,
  warnings: any[]
}

