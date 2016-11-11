#!/usr/bin/env sh

PROTOC="./protoc"
PROTO_ROOT="src/main/jni/cpp"
PROTO_IN_DIR="${PROTO_ROOT}/protocol"
PROTO_OUT_DIR="src/main/java"

# Compile the *.proto file.
echo "Generating .cpp/.hpp from .proto ..."
for proto_file in `find ${PROTO_IN_DIR} -name "*.proto"`
do
    echo "  Compile: ${proto_file}"
    ${PROTOC} --proto_path=${PROTO_ROOT} --java_out=${PROTO_OUT_DIR} ${proto_file}
done
echo "Done!"
