#coding:UTF-8

def makeSnippet(tempStep, temp, variable):
	# ng-class の中身
	print("{", end='')
	for i in range(count):
		if i>0:
			print(",", end='')
		print(variable+str(i)+":", end='');
		if i>0:
			print(variable+" >= "+str(temp),end='')
		if i>0 and i<count-1:
			print("&&", end='')
		temp += tempStep
		if i<count-1:
			print(variable+" < "+str(temp),end='')
	
	print("}")


# 定数
count = 24+1
hslmin = 0
hslmax = 240
tempmin = -6
tempmax = 42

#
hsl = hslmax
hslStep = (hslmax - hslmin) / (count-1)

# スタイルシート部分の snippet
for i in range(count):
	print("      g.t"+str(i)+" { fill: hsl("+str(hsl)+",90%,40%); }")
	hsl -= hslStep

tempStep = (tempmax - tempmin) / (count-1)
makeSnippet( tempStep, tempmin - tempStep, "t00")
makeSnippet( tempStep, tempmin - tempStep, "t10")
makeSnippet( tempStep, tempmin - tempStep, "t01")
makeSnippet( tempStep, tempmin - tempStep, "t11")


