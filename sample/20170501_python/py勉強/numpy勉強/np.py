#coding:UTF-8

# お勉強

import numpy as np

def kugiri(n, *args):
	m = n//2
	n = n - m
	if m > 0:
		print(u"\n"*m)
	print(u"-"*60)
	if len(args) > 0:
		print(args[0])
		print(u"-"*60)
	if n > 0:
		print(u"\n"*n)
#
#　行　列
#

# 4行3列の行列
a = np.array([range(1,4),range(4,7),range(7,10),range(10,13)])

# 表示。行列的に表示された
print(u"print(a) の結果")
print(a)

kugiri(0)
print(u"フラグを表示")
print(u"  C_CONTIGUOUS : データがメモリ上に連続しているか")
print(u"  F_CONTIGUOUS : 同上(Fortran配列型)")
print(u"  OWNDATA      : 自分のデータ化、ビューの場合は False")
print(u"  WRITEABLE    : データ変更可能か")
print(u"  ALIGNED      : データ型がアラインされているか")
print(u"  UPDATEIFCOPY : True にできない")
kugiri(0)
print(a.flags)

kugiri(0)
print(u"次元数(ndim) =", a.ndim)

kugiri(0)
print(u"要素数(size) =", a.size)

kugiri(0)
print(u" 形 (shape)  =", a.shape)

kugiri(0)
print(u"1要素のサイズ=", a.itemsize)

kugiri(0)
print(u"strides(メモリ配置) =", a.strides)

kugiri(0)
print(u"バイト数(nbytes) =", a.nbytes)

kugiri(0)
print(u"要素のデータ型(dtype) =", a.dtype)

#
#　乱　数 (メルセンヌツイスタらしい)
#
kugiri(2)
for i in range(10):
	print(np.random.randint(0, 100, 10))

#
#　要　素　に　対　す　る　型　指　定
#
kugiri(2, u"要素に対する型指定")
print(u"要素ごとに違う型を指定できる")
b = np.rec.array([(35, u"man", u"Abe"), (45, u"woman", u"Ito")], dtype=[("age","i4"),("gender","a5"),("name", "S20")])
b0 = b[0]
print(u"年齢:", b0.age, u" 性別:", b0.gender, u"名前:", b0.name)

#
# np.tile()
#
kugiri(2, u"np.tile()を試す")
a = np.array(range(5))
b = np.tile(a, 2)
c = np.tile(a, (5,1))
print(u"a = np.array(range(5)) =", a)
print(u"b = np.tile(a, 2)      =", b)
print(u"c = np.tile(a, (5,1))  :")
print(c)
d = np.random.randint(0, 100, 25)
d = d.reshape((5,5)) # d.resize((5,5)) とやってもよく、この場合、本体が変わる
print(u"d =np.random.randint(0, 100, 5) :")
print(d)
d = c - d
print(u"d = c - np.array(range(2,7))")
print(d)

print(u"d ** 2:")
print(d ** 2)
print(u"sum(axis=1):")
print( (d**2).sum(axis=1) )
print(u"sqrt:")
dist = ((d**2).sum(axis=1))**0.5
print( dist )

ind = dist.argsort()
print(ind)
