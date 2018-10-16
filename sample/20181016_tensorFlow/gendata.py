import sys
import random

# 出力結果を一定にするため、常に同じ乱数の種を指定する
RANDOM_SEED = 314
random.seed(RANDOM_SEED)

# パラメータを指定する
a = 10
b = -4
X_RANGE = (-10, 10)
ERROR_RANGE = (-20, 20)

# 引数の数をチェックし、引数がなかったら終了する
if len(sys.argv) < 2:
    sys.stderr.write("{} <number of output>\n".format(sys.argv[0]))
    sys.exit(-1)

# 引数で指定された数だけループを回す
for n in range(0, int(sys.argv[1])):
    # x, y, eを計算して出力する
    x = random.random() * (X_RANGE[1] - X_RANGE[0]) + X_RANGE[0]
    y = a * x + b
    e = random.random() * (ERROR_RANGE[1] - ERROR_RANGE[0]) + ERROR_RANGE[0]
    print("{},{}".format(x, y+e))

