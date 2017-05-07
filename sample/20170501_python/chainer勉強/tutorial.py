#coding:UTF-8

import numpy as np
import chainer
from chainer import cuda, Function, gradient_check, report, training, utils, Variable
from chainer import datasets, iterators, optimizers, serializers
from chainer import Link, Chain, ChainList
import chainer.functions as F
import chainer.links as L
from chainer.training import extensions

x_data = np.array([5], dtype=np.float32)
x = Variable(x_data)
y = x**2 - 2 * x + 1
print("y=",y)
print("y.data=",y.data)
print("x.data=",x.data)
y.backward()
print("x.grad(after backward)=", x.grad)
