# Chainer を試してみる

2017/5/2 信吾PC(Ubuntu16.04.1 LTS (GNU/Linux 4.4.0-47-generic x86_64))に chainer を入れて遊ぶ。

## docker

まず、basic-ubu を元ネタにして起動。

yusuke でログインし、`docker run --name chainer -it basic-ubu /bin/bash`。

### python3 インストール

jdk1.8はあるが、python がないのでインストールする。

```
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install python3
```

python3.5.2 がインストールされた。コマンドはpython ではなく、python3 であることに注意。pip もインストールする。

```
sudo apt-get install python3-pip
```

### chainer インストール

```
pip3 install chainer
```

バージョンが8.1.1で古く、9.0.1 がある、と言われたのでアップグレード。

```
pip3 install --upgrade pip
```
