# 主成分分析 Principal Component Analysis

import pandas as pd
import numpy as np

df_wine = pd.read_csv('https://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data',
				header=None)
from sklearn.cross_validation import train_test_split
from sklearn.preprocessing import StandardScaler

# 2列目以降のデータを X に、1列目のデータを y に格納
X, y = df_wine.iloc[:, 1:].values, df_wine.iloc[:, 0].values

# トレーニングデータとテストデータに分割
X_train, X_test, y_train, y_test = train_test_split(
					X, y, test_size=0.3, random_state=0)

# 平均と標準偏差を用いて標準化
sc = StandardScaler()
X_train_std = sc.fit_transform(X_train)
X_test_std = sc.transform(X_test)

# 共分散行列を作成
cov_mat = np.cov(X_train_std.T)

# 固有値と固有ベクトルを計算
eigen_vals, eigen_vecs = np.linalg.eig(cov_mat)

print('\nEigenvalues\n%s' % eigen_vals)
