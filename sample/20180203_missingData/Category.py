import pandas as pd

# サンプルデータを生成
df = pd.DataFrame([
	['green', 'M', 10.1, 'class1'],
	['red', 'L', 13.5, 'class2'],
	['blue', 'XL', 15.3, 'class1']])

# 列名を指定
df.columns = ['color', 'size', 'price', 'classlabel']
print(df)
input('press return')

#-------------------------------------

# T シャツのサイズと整数を対応させるディクショナリを生成
size_mapping = {'XL':3, 'L':2, 'M':1}

# T シャツのサイズを整数に変換
df['size'] = df['size'].map(size_mapping)
print("Tシャツのサイズを指定した数値に変換")
print(df)
input('press return')

#-------------------------------------
import numpy as np

# クラスラベルと整数を対応させるディクショナリを生成
print("-- np.unique(df['classlabel'])")
print(np.unique(df['classlabel']))

print("-- enumerate で(idx, label)を順次表示")
for idx, label in enumerate(np.unique(df['classlabel'])):
	print(idx, label)

class_mapping = {label:idx for idx, label in
	enumerate(np.unique(df['classlabel']))}

print("-- class_mapping")
print(class_mapping)

# クラスラベルを整数に変換
df['classlabel'] = df['classlabel'].map(class_mapping)
print("クラスラベルを自動的に整数化")
print(df)

# 整数をクラスラベルを対応させるディクショナリ(inverse)を生成
inv_class_mapping = {v: k for k, v in class_mapping.items()}

# 整数からクラスラベルに変換
df['classlabel'] = df['classlabel'].map(inv_class_mapping)
print("クラスラベルに戻す")
print(df)

input('press return')

#-------------------------------------
from sklearn.preprocessing import LabelEncoder

# ラベルエンコーダのインスタンスを生成
class_le = LabelEncoder()

# クラスラベルから整数に変換
y = class_le.fit_transform(df['classlabel'].values)
print("scikit learn の LabelEncoder でクラスラベルを整数化")
print(y)
df['classlabel'] = y
print(df)

print("inverse_transform で戻すこともできる")
df['classlabel'] = class_le.inverse_transform(y)
print(df)
