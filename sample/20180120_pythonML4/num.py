import numpy as np

a = np.arange(0,100)
print(type(a))

b = []
for i in range(0,100):
	c = []
	for j in range(0,3):
		c.append(j)
	b.append(c)

d = np.array(b)
print(type(d))
print(d)
