Machine Learning Library
========================

It is powered by the MxNet.

####Prerequisite:

* NDK (for generating the toolchain) - [website](https://developer.android.com/ndk/)
* MxNet (Machine Learning framework) - [website](https://github.com/dmlc/mxnet)
* OpenBlas (Basic Linear Algebra Subprograms) - [website](http://www.openblas.net/)

#### Create a Toolchain by NDK

If you use AndroidStudio's NDK, it is usually installed in `/Users/$USER/Library/Android/sdk/ndk-bundle`. There are eveything necessary for generating a toolchain, the only command you need to run is:

```sh
$NDK/build/tools/make_standalone_toolchain.py \
    --arch arm --api 16 --stl=gnustl --install-dir /tmp/ndk-toolchain-arm-16
```

> "--arch arm" will generate "armeabi" and "armeabi-v7a" toolchains.

##### arch option:
`arm`, `arm64`, `mips`, `mips64`, `x86`, `x86_64`.

[official instruction](https://developer.android.com/ndk/guides/standalone_toolchain.html#creating_the_toolchain)

#### Build OpenBlas

```sh
brew install openblas
```

#### Build MxNet

First, you need to download MxNet codes.

```sh
git clone https://github.com/dmlc/mxnet.git
```

And then, add toolchain to the system **PATH**.

```sh
export PATH=/tmp/ndk-toolchain-arm-16/bin:$PATH
```

Add arm toolchain if you want to build a arm shared library.

```sh
export CC=arm-linux-androideabi-gcc
export CXX=arm-linux-androideabi-g++
```

TODO: Add arm-v7 toolchain if you want to build a arm shared library.

```sh
```
