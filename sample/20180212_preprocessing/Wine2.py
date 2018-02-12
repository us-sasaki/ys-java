from SBS import SBS
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

columns = columns_ja
df_wine.columns = columns

# クラスラベルを表示
print('Class labels', np.unique(df_wine[columns[0]]))

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

#---------------------------------------
#  KNN分類器を使ってSBS実装の効果を確認
from sklearn.neighbors import KNeighborsClassifier
import matplotlib.pyplot as plt
# k近傍分類器のインスタンスを生成(近傍点数=2)
knn = KNeighborsClassifier(n_neighbors=2)
# 逐次後退選択のインスタンスを生成(特徴量の個数が１になるまで特徴量を選択)
sbs = SBS(knn, k_features=1)
# 逐次後退選択を実行
sbs.fit(X_train_std, y_train)

# 近傍点の個数のリスト(13, 12, ..., 1)
k_feat = [len(k) for k in sbs.subsets_]
# 横軸を近傍点の個数、縦軸をスコアとした折れ線グラフのプロット
plt.plot(k_feat, sbs.scores_, marker='o')
plt.ylim([0.7, 1.1])
plt.ylabel('Accuracy')
plt.xlabel('Number of features')
plt.grid()
plt.show()

# 13個中、5個の特徴量で全問正解になった。
print('グラフより、13個中、5個の特徴量で全問正解になった。その5個')
k5 = list(sbs.subsets_[8])
print(df_wine.columns[1:][k5])

# 13個すべての特徴量を用いてモデルに適合
knn.fit(X_train_std, y_train)
# トレーニングの正解率を出力
print('knn で13個すべての特徴量を使って学習させた場合')
a1 = knn.score(X_train_std, y_train)
print('Training accuracy:', a1)
# テストの正解率を出力
a2 = knn.score(X_test_std, y_test)
print('Test accuracy:', a2)
print('テストとトレーニングの差:', (a1-a2))

print('knn で5個に絞った場合')
knn.fit(X_train_std[:,k5], y_train)
a1 = knn.score(X_train_std[:,k5], y_train)
print('Training accuracy:', a1)
# テストの正解率を出力
a2 = knn.score(X_test_std[:,k5], y_test)
print('Test accuracy:', a2)
print('テストとトレーニングの差:', (a1-a2))
