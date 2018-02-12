import numpy as np
import pandas as pd

# wine データセットを読み込む
df_wine = pd.read_csv(
	'https://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data',
	header=None)

# 列名を設定
columns = ['Class label', 'Alcohol', 'Malic acid', 'Ash',
	'Alcalinity of ash', 'Magnesium', 'Total Phenols', 'Flavanoids',
	'Nonflavanoid phenols', 'Proanthocyanins', 'Color intensity', 'Hue',
	'OD280/OD315 of deluted wines', 'Proline']
columns_ja = ['クラスラベル', 'アルコール', 'リンゴ酸', '灰分',
	'灰分アルカリ度', 'マグネシウム', 'フェノール総量', 'フラボノイド',
	'フラボノイド以外のフェノール', 'プロアントシアニン', '色彩強度', '色合い',
	'希釈ワインの OD280/OD315', 'プロリン']

#columns = columns_ja
df_wine.columns = columns

# クラスラベルを表示
print('Class labels', np.unique(df_wine[columns[0]]))

# wine データセットの先頭５行を表示
print('wine データセットの先頭５行')
print(df_wine.head())

#print('wine データ')
#print(df_wine)

# トレーニングデータとテストデータに分割する
print('トレーニングデータとテストデータに分割する')

from sklearn.cross_validation import train_test_split

# 特徴量とクラスラベルを分離
# iloc は pandas.DataFrame のメソッドで、番号指定の部分行列取得
# http://ailaby.com/lox_iloc_ix/
# values は np 配列表現の取得
X, y = df_wine.iloc[:, 1:].values, df_wine.iloc[:, 0].values

# トレーニングデータとテストデータに分割する
# 全体の 30 % をテストデータにする
X_train, X_test, y_train, y_test = \
	train_test_split(X, y, test_size=0.3, random_state=0)

#-----------------------
# 正規化(normalization)
#
# MinMaxScaler で、最大、最小をそろえる(normalize, 正規化)
from sklearn.preprocessing import MinMaxScaler

mms = MinMaxScaler()

# トレーニングデータをスケーリング
X_train_norm = mms.fit_transform(X_train)

# テストデータをスケーリング
X_test_norm = mms.transform(X_test)

print("正規化されたトレーニングデータ")
print(X_train_norm)

#-------------------------
# 標準化(standardization)
from sklearn.preprocessing import StandardScaler

# 標準化のインスタンスを生成(平均 = 0、標準偏差 = 1 に変換)
stdsc = StandardScaler()
X_train_std = stdsc.fit_transform(X_train)
X_test_std = stdsc.transform(X_test)

print("標準化されたトレーニングデータ")
print(X_train_std)
input('press return')

#--------------------------------------
# ロジスティック回帰に L1 正則化を適用
print('ロジスティック回帰に L1 正則化を適用')

from sklearn.linear_model import LogisticRegression
# L1 正則化ロジスティック回帰のインスタンスを生成(逆正則化パラメータ C = 0.1)
lr = LogisticRegression(penalty='l1', C=0.1)

# トレーニングデータに適合
lr.fit(X_train_std, y_train)

# トレーニングデータに対する正解率の表示
print('Training accuracy:', lr.score(X_train_std, y_train))

# テストデータに対する正解率の表示
print('Test accuracy: ', lr.score(X_test_std, y_test))

# 切片の表示
print('切片の表示')
print(lr.intercept_)

# 重み係数の表示
print('重み係数(sparse(疎)になっている)')
print(lr.coef_)

# グラフに表示
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot()

colors = ['blue', 'green', 'red', 'cyan', 'magenta', 'yellow', 'black',
			'pink', 'lightgreen', 'lightblue', 'gray', 'indigo', 'orange']

weights, params = [], []

for c in np.arange(-4, 6):
	lr = LogisticRegression(penalty='l1', C=10.0**c, random_state=0)
	lr.fit(X_train_std, y_train)
	weights.append(lr.coef_[1])
	params.append(10.0**c)

weights = np.array(weights)

for column, color in zip(range(weights.shape[1]), colors):
	plt.plot(params, weights[:, column], label=df_wine.columns[column+1],
	color=color)

plt.axhline(0, color='black', linestyle='--', linewidth=3)

# 横軸の範囲の設定
plt.xlim([10.0**(-5), 10.0**5])

# 軸のラベルの設定
plt.ylabel('weight coefficient')
plt.xlabel('C')

# 横軸を対数スケールに設定
plt.xscale('log')
plt.legend(loc='upper left')
ax.legend(loc='upper center', bbox_to_anchor=(1.38, 1.03), ncol=1, fancybox=True)

plt.show()
