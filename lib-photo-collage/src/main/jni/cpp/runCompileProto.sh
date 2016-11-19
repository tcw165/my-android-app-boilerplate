#!/usr/bin/env sh

PROJECT_DIR=`pwd`
PROTOC="./protoc"
PROTO_IN_DIR="./assets/protobuf"
PROTO_OUT_DIR="./protocol"

# Compile the *.proto file.
echo "Generating .cpp/.hpp from .proto ..."
for proto_file in `find . -name "*.proto"`
do
    echo "  Compile: ${proto_file}"
    ${PROTOC} --cpp_out=. ${proto_file}
done
echo "Done!"