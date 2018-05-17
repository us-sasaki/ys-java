# coding:UTF-8

import matplotlib.pyplot as plt
import numpy as np
from scipy.special import gamma # gamma function
from scipy.misc import comb # combination

averageCount = 3 # 1 回に0.3回の割合
for n in range(10, 100, 10):
	l = averageCount / n
	
	x = np.arange(1, 10, 10/n)
	y = comb(n, x*n/10) * (l ** (x*n/10)) * ( (1-l) ** (n-(x*n/10)) )
	
	plt.plot(x, y)

plt.xlim(0, 10) # x 軸の範囲を指定
plt.show()

#input()
