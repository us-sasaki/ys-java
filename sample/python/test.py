# coding:UTF-8

import matplotlib.pyplot as plt
import numpy as np
import itertools

plt.plot([3,4,2,1])
plt.ylabel('some numbers')
plt.show() # show は window を閉じるまでブロックする

#input()

x = np.arange(-3, 3, 0.1)
print(x)
y = np.sin(x)
plt.plot(x, y)
plt.show()

#input()

x = np.random.randn(30)
y = np.sin(x) + np.random.randn(30)/4
plt.plot(x, y, "x")
plt.show()

print(2**2028)

s = ['a', 'b', 'c']
c = list(itertools.combinations(s, 2))
print(c)

input()

