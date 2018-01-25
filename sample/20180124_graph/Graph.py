import matplotlib.pyplot as plt
import numpy as np

x = np.arange(0, 200000)

xx = x[0:10001]
y = np.hstack( (x[0:1001]*1.18,
				x[1001:5001]*0.99	-1001*0.99 + 1001*1.18,
				x[5001:10001]*0.82	+5001*(0.99-0.82) ) )


plt.plot(xx,y)
plt.show()

