import numpy as np
import matplotlib.pyplot as plt
import math
from numpy import pi

# 円を pyplot で描く

vertices = 100 # 頂点の数
theta = np.linspace(0, 2*pi, vertices+1) # 0～2π を等間隔に分割

# theta = theta[:-1] # 最後つなげるため、コメントアウト

print(theta)

x = np.cos(theta)
y = np.sin(theta)

plt.plot(points)
plt.show()
