import numpy as np

a = np.arange(0,100)
print('type of np.arange(0,100) : %s' % type(a))

b = []
for i in range(0,100):
	c = []
	for j in range(0,3):
		c.append(j)
	b.append(c)

print('type of b = [[0,1,2],[0,1,2], ..., [0,1,2]] : %s' % type(b))

d = np.array(b)

print('type of type(np.array(b)) : %s' % type(d))
#print(d)
