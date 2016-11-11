# How to use Protocol Buffer

## Protocol Buffer

Protocol buffer contains two part, which are `Protobuf Compiler` and `Protobuf Runtime`.
Use `Protobuf Compiler ` file to compile `.proto` files into different platform-depedent files.
When running your application with protocol buffer, you will aslo have to install `protobuf Runtime `.
Following steps introduce how to download `Protobuf Compiler ` and install `Protobuf Runtime `.

## Download Protobuf Compiler

### iOS

1. Go to [Protocol Buffer v3.0.0 Release](https://github.com/google/protobuf/releases/tag/v3.0.0) download `protobuf-your_platform-3.0.0.zip`

2. Extract the zip file and get the `protoc` file inside `bin` directory.

### Windows

Install `Google.Protobuf.Tools` NuGet package, which contains precompiled version of protoc.exe.

## Compile the .proto files with Protobuf Compiler

Following is a sample of using `protoc` to compile a `.proto` file.
Change `--objc_out` to your platform, like `--csharp_out` for C#.

```
./protoc --objc_out=output_path SomeFile.proto
```

## Install Protocol Buffer Runtime

### iOS

Add following to Podfile

```
pod 'Protobuf', '3.0.0'
```

### Windows

Install `Google.Protobuf` NuGet package.

# How to Use PicCollage Algorithm Library

## The Cpp files needs to be compiled

- `/algorithm` : all files.
- `/common` : all `.cpp` files.
- `/include` : all files.
- `/protocol` : all `.h` and `.cc` files.
- `/third_party` : all files.

## Add C++ Flags

Following are the C Flags to add.
Note that `${PIC_ALGO_LIB_ROOT_PATH}` represents the root path where you put the `pic-collage-algorithms-lib` project.

```
-fexceptions
-frtti
-DHAVE_PTHREAD
-I${PIC_ALGO_LIB_ROOT_PATH}/third_party/
-I${PIC_ALGO_LIB_ROOT_PATH}
```

## Compile the .proto files with Protobuf Compiler

Use `protoc` to compile all `.proto` files in `/protocol` directory of [pic-collage-algorithms-lib](https://github.com/my/pic-collage-algorithms-lib)

Sample code of iOS, [runCompileProto.sh](https://github.com/my/CBGridGenerator/blob/master/runCompileProto.sh).

``` shell
PROTOC="./protoc"
PROTO_ROOT="./PicCollageCppLib"
PROTO_IN_DIR="${PROTO_ROOT}/protocol"
PROTO_OUT_DIR="./CBGridGenerator/Classes"

# Compile the *.proto file.
echo "Generating .cpp/.hpp from .proto ..."
for proto_file in `find ${PROTO_IN_DIR} -name "*.proto"`
do
    echo "  Compile: ${proto_file}"
    ${PROTOC} --proto_path=${PROTO_ROOT} --objc_out=${PROTO_OUT_DIR} ${proto_file}
done
echo "Done!"
```

Put the compiled files into your project, the files will look like following in iOS. (The file type will differ according to different output language)

```
ProtoGrid.pbobjc.h
ProtoGrid.pbobjc.m
ProtoGridList.pbobjc.h
ProtoGridList.pbobjc.m
ProtoPhoto.pbobjc.h
ProtoPhoto.pbobjc.m
ProtoPhotoList.pbobjc.h
ProtoPhotoList.pbobjc.m
ProtoRectF.pbobjc.h
ProtoRectF.pbobjc.m
ProtoRectSlot.pbobjc.h
ProtoRectSlot.pbobjc.m
```
