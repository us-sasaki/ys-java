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

#------------------------------------------
# Random Forest の特徴量の重要性パラメータ
#
print('Random Forest によりモデルを生成します')
from sklearn.ensemble import RandomForestClassifier

# Wine データセットの特徴量の名称
feat_labels = df_wine.columns[1:]

# ランダムフォレストオブジェクトの生成
# (木の個数=10000, すべてのコアを用いて並列計算を実行)
forest = RandomForestClassifier(n_estimators=10000, random_state=0, n_jobs=-1)

# モデルに適合
forest.fit(X_train, y_train)

# 特徴量の重要度を抽出
importances = forest.feature_importances_

# 重要度の降順で特徴量のインデックスを抽出
indices = np.argsort(importances)[::-1]

# 重要度の降順で特徴量の名称、重要度を表示
for f in range(X_train.shape[1]):
	print("%2d) %-*s %f" %
		(f+1, 30, feat_labels[indices[f]], importances[indices[f]]))

input('press return')

# グラフ表示
import matplotlib.pyplot as plt
# 日本語フォント設定
from matplotlib.font_manager import FontProperties
fp = FontProperties(fname=r'C:\WINDOWS\Fonts\MSGOTHIC.TTC', size=12)
#plt.rcParams['font.family'] = 'MSGOTHIC'
#plt.rcParams['font.size'] = 20 #フォントサイズを設定

plt.title('Feature Importances')
plt.bar(range(X_train.shape[1]), importances[indices], color='lightblue',
		align='center')
plt.xticks(range(X_train.shape[1]), feat_labels[indices], rotation=-90, fontproperties=fp)
plt.xlim([-1, X_train.shape[1]])
plt.tight_layout()
plt.show()

