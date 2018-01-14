# coding:UTF-8
#
# Iris データをhold out(トレーニングデータの特定のデータをテストデータ
# にする)でパーセプトロンで学習
#
# 2018/1/13
#

from sklearn import datasets
import numpy as np

# Iris データは sklearn に入っている
iris = datasets.load_iris()

# 説明変数は3,4列目
X = iris.data[:,[2,3]]

# 目的変数(Iris のクラスラベル)
y = iris.target
print("分類ラベル数:", np.unique(y))

from sklearn.cross_validation import train_test_split

# 全体の 30% をテストデータとする（トレーニングデータは 70%）
# random_state は分類乱数の種(再現性を持たせるために 0 を指定)
X_train, X_test, y_train, y_test = train_test_split(
X, y, test_size=0.3, random_state=0)

# 
from sklearn.preprocessing import StandardScaler

# データのμ(平均値)とσ(標準偏差)が同じになるように正規化する
# StandardScaler の機能
sc = StandardScaler()
sc.fit(X_train)

# 正規化実行
X_train_std = sc.transform(X_train)
X_test_std = sc.transform(X_test)

# Perceptron クラス
from sklearn.linear_model import Perceptron

# エポック数40, 学習率 0.1 でインスタンス生成
ppn = Perceptron(n_iter=40,eta0=0.1,random_state=0,shuffle=True)

# 学習(一瞬で終わる)
ppn.fit(X_train_std, y_train)

# 学習されたモデル(ppn)で予測させる
y_pred=ppn.predict(X_test_std)

# 誤りがどのくらいあるか表示させる(4 となる)
print('ご分類されたサンプル数: %d' % (y_test != y_pred).sum())

# 可視化
X_combined_std = np.vstack((X_train_std, X_test_std))

y_combined = np.hstack((y_train, y_test))

# 自作ファイルから関数をインポート
from ClassPlot import plot_decision_regions
import matplotlib.pyplot as plt

plot_decision_regions(X=X_combined_std, y=y_combined, classifier=ppn,
						test_idx=range(105,150))

plt.xlabel('petal length [standardized]')
plt.ylabel('petal width [standardized]');

plt.legend(loc='upper left')
plt.show()

# 予測の正解率を計算
from sklearn.metrics import accuracy_score

# 表示
print('正解率: %.2f' % accuracy_score(y_test, y_pred))
