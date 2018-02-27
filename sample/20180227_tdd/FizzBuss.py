# coding:UTF-8

for i in range(1,100):
	s = str(i)
	if i%3 == 0:
		s ='Fizz', end='')
	elif i%5 == 0:
		print('Buzz', end='')
	else:
		print(i, end='')
	print()

