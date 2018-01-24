# coding:UTF-8
#
# Iris データを表示する
#
#

from sklearn import datasets
import numpy as np

# Iris データは sklearn に入っている
iris = datasets.load_iris()

# 説明変数
print(iris.data)

# 目的変数
print(iris.target)



