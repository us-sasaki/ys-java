#
# 積み上げ式の課金モデル(c8y)をグラフ化する
#
import matplotlib.pyplot as plt
import numpy as np
import math

x = np.arange(0, 2000000)
y = np.arange(0, 2000000)

price = 0

for i in range(2000000):
	y[i] = price
	if i <= 1000:
		price += 1.18
	elif i <= 5000:
		price += 0.99
	elif i <= 10000:
		price += 0.82
	elif i <= 25000:
		price += 0.65
	elif i <= 50000:
		price += 0.48
	elif i <= 100000:
		price += 0.32
	elif i <= 250000:
		price += 0.17
	elif i <= 500000:
		price += 0.11
	elif i <= 1000000:
		price += 0.08
	else:
		price += 0.06

plt.plot(x,y)

xx = x[1:]
z = (3.05 * xx) -0.17*(xx * np.log(xx) - xx)/math.log(2)
plt.plot(xx,z)

plt.show()

# 傾きは x が 2 倍になると a 小さくなる
# 傾きが log2(x) に比例
# dy/dx = b - a log2(x) = b - a log(x)/log(2)
# y = bx - a*(x log(x) - x)/log(2) + C
# a = 0.17 とする

yz = y[1:] - z
plt.plot(xx,yz)
plt.show()
