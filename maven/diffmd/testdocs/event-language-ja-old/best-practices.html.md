---
order: 40
title: Best practices
layout: default
toc: true
---

## 文の命名

@Name アノテーションにより、モジュールの文に名前をつけることができます。単一モジュール内で名前は一意である必要があります。[[リアルタイム通知]] のチャネルで、このことは直接関係します。チャネル名(ひいては文の名前)はリストに表示されるため、管理UIでのモジュールのデバックに有用です。文に命名しない場合、自動的に "statement_{文番号}" と命名されます。

## デバイスコンテキストの利用

デバイスコンテキストを必要とする場合、コンテキストにすべての文を設定する必要は通常ありません。例えば、measurement のとりまとめを行う場合、ほとんどの場合、実際のとりまとめを行う文のコンテキストを必要とするのみです。初めにコンテキストを全く含まないモジュールを作成し、必要となる文の最後にコンテキストを追加して下さい。

## モジュールの分割

モジュールが本当に大きくなってきたら、複数のモジュールに分割するのが便利です。スキーマや関数のようなものは、宣言されるとテナントのすべてのモジュールで利用できます。よい方法は、

* モジュール１：入力データをフィルターし、データベースからのデータを追加
* モジュール２：計算
* モジュール３：データベースにデータを生成

モジュール間に依存関係ができることを忘れないでください(例えば、モジュール２はモジュール１で定義されたスキーマを必要とします)。循環依存は避けなければなりません。

## 数値フォーマット

デフォルトでは、measurement を操作する場合、値は BigDecimal (例えば getNumber() を使う場合)とする必要があります。 BigDecimal の計算では、結果が循環小数の場合にエラーとなることがあります。avg() のような組み込み関数では、この場合 null 値を返却します。これを回避するには２つの方法があります。

1. 組み込み関数を使っている場合、一番簡単な方法は BigDecimal を double 値にキャストします

    avg(cast(getNumber(e, "c8y_TemperatureMeasurement.T.value"), double))

2. 自分で計算ロジックを書いている場合(表現内など) BigDecimal型を保ちたい場合、確実に丸めるか切り捨てるかする必要があります。

    getNumber(e, "c8y_TemperatureMeasurement.T.value").divide(new BigDecimal(3), 5, RoundingMode.HALF_UP)