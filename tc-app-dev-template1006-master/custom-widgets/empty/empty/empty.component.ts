import { Component, Input, OnInit, ViewChild, ElementRef } from "@angular/core";
import { IAlarm, AlarmStatus, IResultList } from "@c8y/client";
import { EmptyService } from "../empty.service";
import { C8yService,AlarmFilter } from "../../common/c8y/c8y.service";

@Component({
  selector: "empty",
  templateUrl: "./empty.component.html",
  styleUrls: ["./empty.component.css"],
})
export class Empty implements OnInit {
  @Input() config;

  constructor(
    private emptyService: EmptyService,
    // private c8yService: C8yService
  ) {}
  
  ngOnInit(): void {
    // テスト実装
    console.log('Empty:config.device', this.config.device);
    this.emptyService.f(this.config);
  }

}
