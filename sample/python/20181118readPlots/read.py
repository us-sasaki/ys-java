import json

f = open('GPSLog181028_tanzawa.json', 'r') # open('test.json', 'r') #
j = json.load(f)

c = 0
for plot in j:
	if ('photoFile' in plot):
		c+=1
		print(plot.get('photoFile'))

print(c)

	
	
