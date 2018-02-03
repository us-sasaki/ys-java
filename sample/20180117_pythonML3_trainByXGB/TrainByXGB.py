# coding:UTF-8

import xgboost as xgb
from sklearn.model_selection import GridSearchCV
from sklearn import datasets
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

df = pd.read_csv('source.csv');
print(df)

X=df[['電車名','金沢気温','金沢降水量','金沢天気','金沢日照時間','富山気温','富山降水量','富山天気','富山日照時間','富山日射量']][:]

y=df['富山台車雪量'][:]


print("分類ラベル数:", np.unique(y))

from sklearn.cross_validation import train_test_split

# 全体の 30% をテストデータとする（トレーニングデータは 70%）
# random_state は分類乱数の種(再現性を持たせるために 0 を指定)
X_train, X_test, y_train, y_test = train_test_split(
X, y, test_size=0.3, random_state=0)

# 
from sklearn.preprocessing import StandardScaler

# データのμ(平均値)を０にし、σ(標準偏差)が同じになるように正規化する
# StandardScaler の機能
sc = StandardScaler()
sc.fit(X_train)

# 正規化実行
X_train_std = sc.transform(X_train)
X_test_std = sc.transform(X_test)

# 可視化
X_combined_std = np.vstack((X_train_std, X_test_std))
X_combined = np.vstack((X_train, X_test))
y_combined = np.hstack((y_train, y_test))

#---------------------------------------------------------------------
# XGBoosting のインスタンスを生成
clf = xgb.XGBRegressor()

# ハイパーパラメータ探索
clf_cv = GridSearchCV(clf, {'max_depth':[2,4,6,8,10],
						'n_estimators':[6,25,100,200,300]}, verbose=1)
clf_cv.fit(X_train, y_train)
print(clf_cv.best_params_, clf_cv.best_score_)

# 改めて最適パラメータで学習
clf = xgb.XGBClassifier(**clf_cv.best_params_)
clf.fit(X_train, y_train)

# 決定境界をプロット
"""
from ClassPlot import plot_decision_regions
plot_decision_regions(X_combined, y_combined, classifier=clf,
						test_idx=range(105,150))

# 軸のラベルを設定
plt.xlabel('petal length [standardized]')
plt.ylabel('petal width [standardized]')

plt.legend(loc='upper left')

plt.show()
"""
#------------------------------------------
# 正解率を表示
from sklearn.metrics import accuracy_score

# 表示
y_pred = clf.predict(X_test)
print(y_pred-y_test)
#print('正解率: %.2f' % accuracy_score(y_test, y_pred))
