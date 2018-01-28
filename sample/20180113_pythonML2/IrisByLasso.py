from sklearn.linear_model import Lasso
from sklearn import datasets
import numpy as np
import matplotlib.pyplot as plt

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

# 可視化
X_combined_std = np.vstack((X_train_std, X_test_std))
y_combined = np.hstack((y_train, y_test))

#-----------------------------------------
# Lasso回帰のインスタンスを生成
lasso = Lasso(alpha=1.0)

# トレーニングデータをモデルに適合させる
lasso.fit(X_train_std, y_train)

# 決定境界をプロット
from ClassPlot import plot_decision_regions
plot_decision_regions(X_combined_std, y_combined, classifier=lasso,
						test_idx=range(105,150))

# 軸のラベルを設定
plt.xlabel('petal length [standardized]')
plt.ylabel('petal width [standardized]')

plt.legend(loc='upper left')

plt.show()

#------------------------------------------
# 正解率を表示
from sklearn.metrics import accuracy_score

# 表示
y_pred = lasso.predict(X_test_std)
print('正解率: %.2f' % accuracy_score(y_test, y_pred))
