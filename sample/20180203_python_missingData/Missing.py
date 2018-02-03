import pandas as pd
from io import StringIO

# 欠損値を持つサンプルデータを作成
csv_data = '''A,B,C,D
1.0,2.0,3.0,4.0
5.0,6.0,,8.0
10.0,11.0,12.0,'''

df = pd.read_csv(StringIO(csv_data))
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

print('\n欠損値を含む行を削除')
print(df.dropna()) # axis=0

print('\n欠損値を含む列を削除')
print(df.dropna(axis=1))
