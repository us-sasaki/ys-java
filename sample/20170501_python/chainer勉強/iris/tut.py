#coding:UTF-8
#
# Chainer 1.5 チュートリアルをもとに iris をためす
# iris データ(あやめの種類の推定)を利用
#
# 【引用先】
# http://www.iandprogram.net/entry/chainer_japanese
#

#
# import 呪文
#
import numpy as np
import chainer
from chainer import cuda, Function, gradient_check, report, training, utils, Variable
from chainer import datasets, iterators, optimizers, serializers
from chainer import Link, Chain, ChainList
import chainer.functions as F
import chainer.links as L
from chainer.training import extensions

# 自分で追加した import
from chainer.datasets import tuple_dataset
import numpy.random as nr # iris_data のシャッフルは random.shuffle だとだめ
import csv

# Chainerはほとんどの計算において現在32ビットの浮動小数点のみをサポートします
# こんな感じ：
# x_data = np.array([5], dtype=np.float32)

#
# データ(iris.data)の読み込み。日本語 CSV は、UTF-8/cp932 いずれもうまくいかず。
# iris_data はタプルのリストとして取得される。
#
#iris_data = np.loadtxt('iris.data', delimiter=",", skiprows=0,
#				dtype=[('c0',np.float32),('c1',np.float32),('c2',np.float32),('c3',np.float32),('c4','U20')])
#
# csv を使って、python リストとして読み込む
#
iris_data = []
csvfile = open("iris.data") #, delimiter=",")
for row in csv.reader(csvfile):
	row_data = []
	for data in row:
		row_data.append(data)
	iris_data.append(row_data)

#
# iris データに依存する定数定義
#
INPUT_UNITS = 4		# 入力パラメータは４つ
HIDDEN_UNITS = 20	# 隠れ層 10個にしてみる
OUT_UNITS = 3		# 出力パラメータは３つ(３つに分類するから)
EPOCHS = 20

#-------------------------------------------------
# train_mnist の class MLP をまねて、クラスを作る
#
class YS(Chain):

	#-------------
	# constructor
	#
    def __init__(self, n_units, n_out):
        super(YS, self).__init__(
            # the size of the inputs to each layer will be inferred
            l1=L.Linear(INPUT_UNITS, n_units),  # n_in -> n_units
            l2=L.Linear(n_units, n_units),  # n_units -> n_units
            l3=L.Linear(n_units, n_out),  # n_units -> n_out
        )

	#------------------------
	# 活性化関数 ReLU を定義
	#
    def __call__(self, x):
        h1 = F.relu(self.l1(x))
        h2 = F.relu(self.l2(h1))
        return self.l3(h2)

#model = YS(HIDDEN_UNITS, OUT_UNITS)
#optimizer = optimizers.SGD()
#optimizer.setup(model)

# 訓練データを設定する

#model.zerograds()

# データを整形
#nr.shuffle(iris_data)

all_data = []
all_label = []
labels = []

#print(iris_data[0])
for d in iris_data:
#	print(d)
#	print(type(d[0]))
	all_data.append(np.array(d[0:INPUT_UNITS], dtype=np.float32))
	dlabel = d[INPUT_UNITS]
	if not dlabel in labels:
		labels.append(dlabel)
	all_label.append(labels.index(dlabel))

#print(all_label)
#print(labels)

thres = len(iris_data) * 9 // 10 # データの最初９割は教師、残りはテスト
#print(u"教師データ数",thres)
train = tuple_dataset.TupleDataset(all_data[0:thres], all_label[0:thres])
test  = tuple_dataset.TupleDataset(all_data[thres:], all_label[thres:])

#print(train[0])

# 訓練データセットを毎ループでシャッフル
train_iter = iterators.SerialIterator(train, batch_size=20)
test_iter = iterators.SerialIterator(test, batch_size=20, repeat=False, shuffle=False)

# Classifier により、損失と精度評価関数を定義
# 数字の推定と、アヤメの種類の推定は同種の分類のため、変更しない
# http://www.iandprogram.net/entry/chainer_japanese のまんま
class Classifier(Chain):
	def __init__(self, predictor):
		super(Classifier, self).__init__(predictor=predictor)

		def __call__(self, x, t):
			y = self.predictor(x)
			loss = F.softmax_cross_entropy(y, t)
			accuracy = F.accuracy(y, t)
			report({'loss': loss, 'accuracy': accuracy}, self)
			return loss


model = L.Classifier(YS(HIDDEN_UNITS, OUT_UNITS))
optimizer = optimizers.SGD()
optimizer.setup(model)
model.zerograds()

updater = training.StandardUpdater(train_iter, optimizer)
trainer = training.Trainer(updater, (EPOCHS, 'epoch'), out="tut_result")

# 計算過程を表示させる
trainer.extend(extensions.Evaluator(test_iter, model))

# 報告された値を蓄積し、ログファイルを出力します
trainer.extend(extensions.LogReport())

# ログファイルを出力します
trainer.extend(extensions.PrintReport(['epoch','main/accuracy','validation/main/accuracy']))

# 進捗状況を可視化します
trainer.extend(extensions.ProgressBar())
trainer.extend(extensions.dump_graph('main/loss'))

# Save two plot images to the result dir
if extensions.PlotReport.available():
    trainer.extend(
        extensions.PlotReport(['main/loss', 'validation/main/loss'],
                              'epoch', file_name='loss.png'))
    trainer.extend(
        extensions.PlotReport(
            ['main/accuracy', 'validation/main/accuracy'],
            'epoch', file_name='accuracy.png'))

trainer.run()

# main/accuracy , validation/main/accuracy の意味がわからない
