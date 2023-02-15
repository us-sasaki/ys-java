import { Injectable } from "@angular/core";
import {
  AlarmService,
  AlarmStatus,
  EventService,
  FetchClient,
  InventoryService,
  IFetchResponse,
  IResultList,
  IManagedObject,
  IResult,
  IMeasurement,
  IMeasurementCreate,
  ISeries,
  ISeriesFilter,
  IManagedObjectBinary,
  IAlarm,
  IEvent,
  IdentityService,
  MeasurementService,
  InventoryBinaryService,
  Realtime
} from "@c8y/client";

@Injectable()
export class C8yService {
  private static MEASUREMENT_PAGE_SIZE = 50 as const;
  private static ALARM_PAGE_SIZE = 50 as const;
  private static EVENT_PAGE_SIZE = 50 as const;

  constructor(
    private measurementService: MeasurementService,
    private alarmService: AlarmService,
    private eventService: EventService,
    private identityService: IdentityService,
    private inventoryService: InventoryService,
    private inventoryBinaryService: InventoryBinaryService,
    private fetchClient: FetchClient
  ) {}

  /**
   * デバイスの Measurement 情報を取得する
   *
   * @param deviceId デバイスID
   * @param param filter に設定するパラメータ
   *
   * @returns IMeasurement[] の Promise
   */
   getMeasurements(
     param: {
      deviceId?: string,
      filter?: MeasurementFilter
     }
  ): Promise<IResultList<IMeasurement>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter : any = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    return this.measurementService.list(requestFilter);
  }

  /**
   * デバイスの Measurement Series 情報を取得する
   *
   * @param filter Measurementをfilterする条件 (deviceId,dateFrom,dateToは必須)
   *
   * @returns IResult<ISeries> の Promise
   */
  getMeasurementSeries(filter: ISeriesFilter): Promise<IResult<ISeries>> {
    return this.measurementService.listSeries(filter);
  }
  
  /**
   * デバイスの Alarm 情報を取得する
   *
   * @param param {deviceId?, filter?} デバイスID, alarm 取得の際に指定する filter
   *
   * @returns IResultList<IAlarm> の Promise
   */
  getAlarms(param: {
    deviceId?: string;
    filter?: AlarmFilter;
  }): Promise<IResultList<IAlarm>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter : any = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    requestFilter.withTotalPages = true;

    return this.alarmService.list(requestFilter);
  }

  /**
   * デバイスの Event 情報を取得する
   *
   * @param param {deviceId?, filter?} デバイスID, event 取得の際に指定する filter
   *
   * @returns ResultList<IEvent> の Promise
   */
  getEvents(param: {
    deviceId?: string;
    filter?: EventFilter;
  }): Promise<IResultList<IEvent>> {
    // 渡された filter を変更すると解決しづらいバグにつながるため、deep copy
    const requestFilter : any = (param.filter)? JSON.parse(JSON.stringify(param.filter)) : {};
    if (param.deviceId) {
      requestFilter.source = param.deviceId;
    }
    requestFilter.withTotalPages = true;

    return this.eventService.list(requestFilter);
  }


  /**
   * 指定されたデバイスから報告された最後の Measurement を取得する。
   * 最近の Measurement の Promise が返却される。
   *
   * @param deviceId デバイスID
   * @param type measurement の type
   *
   * @returns 最近の IMeasurement の Promise
   */
  getLastMeasurement(
    deviceId: string,
    type: string
  ): Promise<IMeasurement> {
    const filter: any = {
      type: type,
      revert: true,
      dateFrom: "2022-01-01",
      source: deviceId,
      pageSize: 1
    };

    return this.measurementService.list(filter).then(resultList => resultList.data[0]);
  }

  /**
   * MO ID をキーに MO を取得する
   *
   * @param moId MO ID
   *
   * @returns 指定IDの MO 詳細情報
   */
  getParentAssetId(moId: string): Promise<IResultList<IManagedObject>> {
    const query: any = {
      id: moId,
    };
    const filter: any = {
      withParents: true,
    };
    return this.inventoryService.listQuery(query, filter);
  }

  /**
   * MO ID をキーに MO を取得する
   *
   * @param moId MO ID
   *
   * @returns 指定IDの MO 詳細情報
   */
  getMO(moId: string): Promise<IResult<IManagedObject>> {
    return this.inventoryService.detail(moId);
  }

  /**
   * リアルタイムにイベント等を取得するための Realtime オブジェクトを取得する
   *
   * @returns Realtime のインスタンス
   */
  getRealtime(): Realtime {
    return new Realtime(this.fetchClient);
  }

  /**
   * Things Cloud デバイスID、外部IDタイプを指定して外部IDを取得する
   *
   * @param     deviceId デバイスID
   * @param     externalIdType 外部IDタイプ
   * @returns   外部IDで解決する Promise(見つからない場合、null で解決)
   */
  async getExternalId(
    deviceId: string,
    externalIdType: string
  ): Promise<string> {
    const externalIds = await this.identityService.list(deviceId);
    for (const element of externalIds.data) {
      if (element.type === externalIdType) return element.externalId;
    }
    return null;
  }

  /**
   * 複数デバイスの最新 Event 各1件を取得する
   *
   * @param deviceIds デバイスIDの配列
   * @param fragmentType カスタムフラグメント名
   *
   * @returns 各デバイスの最新 Event の配列
   */
  getLatestEvents(deviceIds: string[], fragmentType?: string): Promise<any> {
    const filter: any = {
      dateFrom: "2020-01-01",
      revert: false,
      fragmentType: fragmentType || "com_ImgaeDownloadable",
      pageSize: 1,
      source: "",
    };

    const latestEvents = deviceIds.map((data) => {
      filter.source = data;
      return this.eventService.list(filter);
    });

    return Promise.all(latestEvents);
  }

  /**
   * メジャーメントデータ作成
   * @param data 作成するメジャーメント
   * 
   * @returns 作成したメジャーメント
   */
  createMeasurement(data: Partial<IMeasurementCreate>): Promise<IResult<IMeasurement>> {
    return this.measurementService.create(data);
  }
  
  /**
   * イベントデータ作成
   * @param data 作成するイベント
   * 
   * @returns 作成したイベント
   */
   createEvent(data: IEvent): Promise<IResult<IEvent>> {
    return this.eventService.create(data);
  }
  
  /**
   * 指定アラームID のアラームステータスを更新する
   *
   * @param alarmId アラームID
   * @param alarmStatus アラームステータス
   *
   * @returns 更新後のアラーム
   */
  updateAlarmStatus(
    alarmId: string,
    alarmStatus: AlarmStatus
  ): Promise<IResult<IAlarm>> {
    const entity: Partial<IAlarm> = {
      id: alarmId,
      status: alarmStatus,
    };
    return this.alarmService.update(entity);
  }

  /**
   * 指定された Binary データを inventory の binary として保存します
   *
   * @param binaryData バイナリデータ(画像など)
   *
   * @returns 保存後の IManagaedObjectBinary
   */
  createBinary(binaryData: Buffer | File | Blob): Promise<IResult<IManagedObjectBinary>> {
    return this.inventoryBinaryService.create(binaryData);
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
   * ID をキーに ManaggedObject を取得する
   *
   * @param id ManaggedObject ID
   *
   * @returns 指定IDの ManagedObject 詳細情報
   */
  async getManagedObjectById(id: string): Promise<IManagedObject> {
    return (await this.inventoryService.detail(id)).data;
  }
}

/**
 * getAlarms で利用される filter。
 */
export interface AlarmFilter {
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateFrom?: string;
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateTo?: string;
  /**
   * dateTo または dateFrom とセットで利用する必要がある。
   * true の時、新しい順、false の時、古い順となる。
   */
  revert?: boolean;
  pageSize?: number;
  /** filter する type 値(c8y_ThresholdAlarm など) */
  type?: string;
  source?: string;
  withTotalPages?: boolean;
}

/**
 * getEvents で利用される filter。
 */
 export interface EventFilter {
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateFrom?: string;
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateTo?: string;
  /**
   * dateTo または dateFrom とセットで利用する必要がある。
   * true の時、古い順、false の時、新しい順となる。
   */
  revert?: boolean;
  pageSize?: number;
  fragmentType?: string;
  source?: string;
  withTotalPages?: boolean;
}

/**
 * getMeasurements で利用される filter。
 */
 export interface MeasurementFilter {
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateFrom?: Date;
  /** ISO-8601 形式(2021-05-17T12:00:13.527+09:00 など) */
  dateTo?: Date;
  /**
   * dateTo または dateFrom とセットで利用する必要がある。
   * true の時、古い順、false の時、新しい順となる。
   */
  revert?: boolean;
  pageSize?: number;
  type?: string;
  source?: string;
  withTotalPages?: boolean;
  valueFragmentType?: string;
  valueFragmentSeries?: string;
}
