// Karma configuration
// Generated on Mon Jul 27 2020 10:38:49 GMT+0900 (日本標準時)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: './custom-widgets',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    plugins: [  // 追記箇所
      require("karma-jasmine"),
      require("karma-chrome-launcher"),
      require("karma-safari-launcher"),
      require("karma-jasmine-html-reporter"),
      require("karma-webpack"),
      require("karma-sourcemap-loader")
    ],

    client:{
      clearContext: false
    },

    // list of files / patterns to load in the browser
    files: [
      "../node_modules/zone.js/dist/zone.js",
      "./test.ts"
    ],


    // list of files / patterns to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      "./test.ts": ["webpack", "sourcemap"]
    },

    webpack: { // 追記箇所: webpack の設定
      resolve: {
        extensions: [".ts", ".js"]
      },
      module: {
        rules: [
          { test: /\.ts$/, use: [{ loader: "ts-loader" }] },
          { test: /\.html$/, use: [{ loader: "html-loader" }] }, // 外部htmlファイルの読み込み用
          { test: /\.css$/, loaders: ["to-string-loader", "style-loader", "css-loader"] } // 外部cssファイルの読み込み用
        ]
      },
      mode: "development",
      devtool: 'inline-source-map'
    },



    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress', 'kjhtml'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['Chrome'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity,

    // timeout
    browserNoActivityTimeout: 60000
  })
}
