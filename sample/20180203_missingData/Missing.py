import pandas as pd
from io import StringIO

# 欠損値を持つサンプルデータを作成
csv_data = '''A,B,C,D
1.0,2.0,3.0,4.0
5.0,6.0,,8.0
10.0,11.0,12.0,'''

df = pd.read_csv(StringIO(csv_data))
print(type(df))
print(df)

# 各特徴量の欠損値をカウント

print()
print('df.isnull()')
print(df.isnull())

print()
print('df.isnull().sum()')
print(df.isnull().sum())

# df の実体は ndarray で、values で取得できる
print()
print(type(df.values))
print(df.values)
input('[return]')

#-----------------------------
# 欠損値削除 pasdas DataFrame
#-----------------------------
print('------------------------')
print('元データ')
print(df)

print('------------------------')
print('\n欠損値を含む行を削除')
print(df.dropna()) # axis=0

print('\n欠損値を含む列を削除')
print(df.dropna(axis=1))

print('\nすべての列がNaNである行だけを削除')
print(df.dropna(how='all'))
input('[return]')

print('------------------------')
print('\n非Nan値が 4 つ未満の行を削除')
print(df.dropna(thresh=4))

print('\n特定の列(この場合は \'C\')に NaN が含まれている行だけを削除')
print(df.dropna(subset=['C']))
input('[return]')

#----------------------
# 欠損値補完 (Imputer)
#----------------------
from sklearn.preprocessing import Imputer

print('------------------------')
print(' scikit learn Imputer')

# Imputer インスタンス生成(平均値補完)
imr = Imputer(missing_values='NaN', strategy='mean', axis=0)

# データを適合
imr = imr.fit(df)

# 補完を実行
imputed_data = imr.transform(df.values)
print(imputed_data)

input('[return]')
