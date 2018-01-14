import matplotlib.pyplot as plt
import numpy as np

# ロジット関数の概形をプロットする
#
# logit(x) = log ( x / (1-x) )

def logit(x):
	return np.log( x/(1-x) )

n = 100
x = np.arange(1, n-1)
x = x / n
y = logit(x)

# 垂直線を追加(x=0.5)
plt.axvline(0.5, color='lightgray')

# 水平線を追加(y=0)
plt.axhline(0, color='lightgray')

# 軸のラベルを設定
plt.xlabel('x')
plt.ylabel('logit(x)')

# y 軸の目盛りを追加
plt.yticks(range(-7, 7, 1))

# x, y を plot
plt.plot(x, y, label='logit function')


plt.show()

