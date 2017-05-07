#coding:UTF-8

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cm as cm
from collections import defaultdict

#
# class TrainDataSet
#
class TrainDataSet():

#---------------------
# constructor in java
#
	def __init__(self, data):	# 第一引数の self は必須。
		data = np.array(data)
		
		self.labels = data[:,0] # labels は 0 列の要素を抜き出したもの
		self.data_set = data[:,1:] # data_set は 1 列以降の列を抜き出したもの
	
	#
	# toString() のようなもの？(オブジェクト固有の文字列表現)
	#
	def __repr__(self):
		ret  = repr(self.labels) + "\n"
		ret += repr(self.data_set)
		return ret
	
#----------------------
# インスタンスメソッド
#
	#
	# データ数を返却する(答、pixel(784) で、教師データは 785 
	#
	def get_data_num(self):
		return self.labels.size
	
	#
	# *args は、不定長引数を tuple で args に受け取る。
	# 同様に **kwargs は、不定長引数を dictionary で kwargs に受け取る。
	# 他にも、args=(1,2) とし、func(*args) とすると、func(1,2) となるらしい。
	#
	def get_labels(self, *args):
		if args is None:
			return self.labels
		else:
			return self.labels[args[0]]
	
	#
	def get_data_set(self):
		return self.data_set
	
	#
	def get_data_set_partial(self, *args):
		if args is None:
			return self.data_set
		else:
			return self.data_set[args[0]]
	
	#
	def get_label(self, i):
		return self.labels[i]
	
	#
	def get_data(self, i):
		return self.data_set[i,:]
	
	#
	def get_data(self, i, j):
		return self.data_set[i][j]
	
size = 28
# train_partial.csv は数が少ないが早い。下はかなり時間がかかる。
master_data	= np.loadtxt("train.csv",delimiter=",",skiprows=1)
test_data	= np.loadtxt("test_small.csv",delimiter=",",skiprows=1)

train_data_set = TrainDataSet(master_data)

# 表示してみる(1つ目のデータの答え(=1))
print(train_data_set.get_labels(0))

#
# global method
#
def get_list_sorted_by_val(k_result, k_dist):
	result_dict		= defaultdict(int)
	distance_dict	= defaultdict(float)
	
	# 数字ラベルごとに集計
	for i in k_result:
		result_dict[i] += 1
	
	# 数字ラベルごとに距離の合計を集計
	for i in range(len(k_dist)):
		distance_dict[k_result[i]] += k_dist[i]
	
	# 辞書型からリストに変換（ソートするため）
	result_list = []
	order = 0
	for key, val in result_dict.items():
		order += 1
		result_list.append([key, val, distance_dict[key]])
	
	# ndarray 型に変換
	result_list = np.array(result_list)
	
	return result_list

#
# main
#
k = 5
predicted_list = []		# 数字ラベルの予測値
k_result_list = []		# k 個の近傍リスト
k_distances_list = []	# k 個の数字と識別対象データとの距離リスト

# k 最近傍法の実行
for i in range(len(test_data)):

	# 識別対象データと教師データの差分をとる
	# np.tile(array, cnt) は array を cnt 数だけ繰り返した配列になる
	# np.tile(array, (x, y)) は　行に x 個、列に y 個、ならべた行列になる
	diff_data = np.tile(test_data[i], (train_data_set.get_data_num(),1)) \
					- train_data_set.get_data_set()
	# ↑は要素ごとに差をとっている。またブロードキャストで正方行列化している
	
	sq_data		= diff_data ** 2		# 各要素を2乗して符号を消す
	sum_data	= sq_data.sum(axis=1)	# それぞれのベクトル要素を足し合わせる
	distances	= sum_data ** 0.5		# ルートをとって距離とする
	ind			= distances.argsort()	# 距離の短い順にソートしてその添え字を取り出す
	k_result	= train_data_set.get_labels(ind[0:k])	# 近いものから k 個取り出す
	k_dist		= distances[ind[0:k]]	# 距離情報も k 個取り出す
	
	k_distances_list.append(k_dist)
	k_result_list.append(k_result)
	
	# k 個のデータから数字ラベルで集約した、（数字ラベル，個数，距離）のリストを生成
	result_list = get_list_sorted_by_val(k_result, k_dist)
	candidate = result_list[result_list[:,1].argsort()[::-1]]
	
	counter = 0
	min = 0
	label_top = 0
	
	# 最も数の多い数字ラベルが複数あったらその中で合計距離の小さい方を選択
	result_dict = {}
	for d in candidate:
#		print(u"候補 ", d, u"d [0]", d[0], u" d[1]", d[1], u" d[2]", d[2])
		if d[1] in result_dict:
			result_dict[d[1]] += [(d[0], d[2])]
		else:
			result_dict[d[1]] = [(d[0], d[2])]
	
#	print(u"キー", result_dict.keys())
#	print(u"値　", np.max(list(result_dict.keys())))
	for d in result_dict[np.max(list(result_dict.keys()))]:
		if counter == 0:
			label_top = d[0]
			min = d[1]
		else:
			if d[1] < min:
				label_top = d[0]
				min = d[1]
		counter += 1
		
	# 結果をリストに詰める
	predicted_list.append(label_top)
	
# 結果の表示
print("[Predicted Data List]")
for i in range(len(predicted_list)):
	print ( ("%d" % i) + "\t" + str(predicted_list[i]) )

#print("[Detail Predicted Data List]")
#print("index k units of neighbors, distances for every k units")
#for i in range(len(k_result_list)):
#	print( ("%d" % i) + "\t" + str(k_result_list[i]) + "\t" + str(k_distances_list[i]) )

for i in range(len(predicted_list)):
	a = test_data[i].reshape((28,28))
	plt.imshow(a, cmap=plt.cm.gray)
	plt.title(u"Test Data "+str(i)+" pred=" + str(predicted_list[i]))
	plt.show()
