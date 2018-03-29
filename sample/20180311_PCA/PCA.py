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

# (固有値、固有ベクトル)のタプルのリストを作成
eigen_pairs = [(np.abs(eigen_vals[i]),eigen_vecs[:,i]) for i in range(len(eigen_vals))]

# (固有値、固有ベクトル)のタプルを大きいものから順に並べ替え
eigen_pairs.sort(reverse=True)

w = np.hstack((eigen_pairs[0][1][:, np.newaxis], eigen_pairs[1][1][:,np.newaxis]))
print('Matrix W:\n', w)

print(X_train_std[0].dot(w))

X_train_pca = X_train_std.dot(w)

colors = ['r', 'b', 'g']
markers = ['s', 'x', 'o']

# 「クラスラベル」「点の色」「点の種類」の組み合わせからなるリストを生成してプロット
import matplotlib.pyplot as plt
for l, c, m in zip(np.unique(y_train), colors, markers):
	plt.scatter(X_train_pca[y_train==l, 0], X_train_pca[y_train==l, 1],
	c=c, label=l, marker=m)
plt.xlabel('PC 1')
plt.ylabel('PC 2')
plt.legend(loc='lower left')
plt.show()
