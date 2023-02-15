// This file is required by karma.conf.js and loads recursively all the .spec and framework files
import "reflect-metadata"; // 追記箇所: Polyfills を追加するライブラリ。これを追加しないとエラーになる
import "zone.js/dist/zone-testing";

import { getTestBed } from "@angular/core/testing";
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from "@angular/platform-browser-dynamic/testing";

declare const require: any;

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting()
);
// Then we find all the tests.
const context = require.context("./", true, /\.service\.spec\.ts$/); // ここでマッチする spec files を読みに行く

// And load the modules.
context.keys().map(context);