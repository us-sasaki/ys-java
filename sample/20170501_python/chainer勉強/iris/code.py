#coding:UTF-8
#
# 10時間で chainer の基本を身につける、から引用されているコード
# iris データ(あやめの種類の推定)を利用
# ★★これは、古い書き方のようなので、やめる
# 【10時間で chainer の基本を身につける】
# http://esu-ko.hatenablog.com/entry/2016/03/02/%E3%80%90%E3%83%87%E3%82%A3%E3%83%BC%E3%83%97%E3%83%A9%E3%83%BC%E3%83%8B%E3%83%B3%E3%82%B0%E3%80%9110%E6%99%82%E9%96%93%E3%81%A7Chainer%E3%81%AE%E5%9F%BA%E6%9C%AC%E3%82%92%E8%BA%AB%E3%81%AB%E3%81%A4
# 【引用先】
# http://qiita.com/kenmatsu4/items/7b8d24d4c5144a686412
#
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import fetch_mldata
from chainer import cuda, Variable, FunctionSet, optimizers
import chainer.functions  as F
import sys

plt.style.use('ggplot')

# 確率的勾配降下法で学習させる際の１回分のバッチサイズ
batchsize = 100

# 学習の繰り返し回数
n_epoch   = 20

# 中間層の数
n_units   = 1000

# 学習用データを N個、検証用データを残りの個数と設定
N = 60000
x_train, x_test = np.split(mnist.data,   [N])
y_train, y_test = np.split(mnist.target, [N])
N_test = y_test.size

# Prepare multi-layer perceptron model
# 多層パーセプトロンモデルの設定
# 入力 784次元、出力 10次元
model = FunctionSet(l1=F.Linear(784, n_units),
                    l2=F.Linear(n_units, n_units),
                    l3=F.Linear(n_units, 10))

# Neural net architecture
# ニューラルネットの構造
def forward(x_data, y_data, train=True):
    x, t = Variable(x_data), Variable(y_data)
    h1 = F.dropout(F.relu(model.l1(x)),  train=train)
    h2 = F.dropout(F.relu(model.l2(h1)), train=train)
    y  = model.l3(h2)
    # 多クラス分類なので誤差関数としてソフトマックス関数の
    # 交差エントロピー関数を用いて、誤差を導出
    return F.softmax_cross_entropy(y, t), F.accuracy(y, t)

# Setup optimizer
optimizer = optimizers.Adam()
optimizer.setup(model.collect_parameters())

train_loss = []
train_acc  = []
test_loss = []
test_acc  = []

l1_W = []
l2_W = []
l3_W = []

# Learning loop
for epoch in xrange(1, n_epoch+1):
    print 'epoch', epoch

    # training
    # N個の順番をランダムに並び替える
    perm = np.random.permutation(N)
    sum_accuracy = 0
    sum_loss = 0
    # 0?Nまでのデータをバッチサイズごとに使って学習
    for i in xrange(0, N, batchsize):
        x_batch = x_train[perm[i:i+batchsize]]
        y_batch = y_train[perm[i:i+batchsize]]

        # 勾配を初期化
        optimizer.zero_grads()
        # 順伝播させて誤差と精度を算出
        loss, acc = forward(x_batch, y_batch)
        # 誤差逆伝播で勾配を計算
        loss.backward()
        optimizer.update()

        train_loss.append(loss.data)
        train_acc.append(acc.data)
        sum_loss     += float(cuda.to_cpu(loss.data)) * batchsize
        sum_accuracy += float(cuda.to_cpu(acc.data)) * batchsize

    # 訓練データの誤差と、正解精度を表示
    print 'train mean loss={}, accuracy={}'.format(sum_loss / N, sum_accuracy / N)

    # evaluation
    # テストデータで誤差と、正解精度を算出し汎化性能を確認
    sum_accuracy = 0
    sum_loss     = 0
    for i in xrange(0, N_test, batchsize):
        x_batch = x_test[i:i+batchsize]
        y_batch = y_test[i:i+batchsize]

        # 順伝播させて誤差と精度を算出
        loss, acc = forward(x_batch, y_batch, train=False)

        test_loss.append(loss.data)
        test_acc.append(acc.data)
        sum_loss     += float(cuda.to_cpu(loss.data)) * batchsize
        sum_accuracy += float(cuda.to_cpu(acc.data)) * batchsize

    # テストデータでの誤差と、正解精度を表示
    print 'test  mean loss={}, accuracy={}'.format(sum_loss / N_test, sum_accuracy / N_test)

    # 学習したパラメーターを保存
    l1_W.append(model.l1.W)
    l2_W.append(model.l2.W)
    l3_W.append(model.l3.W)

# 精度と誤差をグラフ描画
plt.figure(figsize=(8,6))
plt.plot(range(len(train_acc)), train_acc)
plt.plot(range(len(test_acc)), test_acc)
plt.legend(["train_acc","test_acc"],loc=4)
plt.title("Accuracy of digit recognition.")
plt.plot()

