#coding:UTF-8

# 和美のジュース倒し問題の検算
# 数学関数を使う
import math

# 問題で定義される定数関係
r = 2.5
d = 0.6
tanT = math.tan(2.0 * math.pi * 40.0 / 360.0)
b = 1.5
phi = math.acos(0.6)
rcosPhi = 2.5 * 3.0 / 5.0
rsinPhi = 2.5 * 4.0 / 5.0

print(u"定数")
print(u"r    =",r)
print(u"d    =",d)
print(u"tanθ=",tanT)
print(u"b    =",b)
print(u"φ   =",phi)
print(u"r cosφ=",rcosPhi)
print(u"r sinφ=",rsinPhi)

print(u"-"*60)

# 体積 V1 を求める
v1 = math.pi * r * r * (b * tanT - d)
print(u"V1=",v1)

# 体積 V2 を求める
v2 = tanT * (- phi * r*r * rcosPhi + r*r * rsinPhi - (1.0/3.0)*(rsinPhi)**3)
print(u"V2=",v2)

# 合計
print(u"答 = V1+V2 = ", (v1+v2))
