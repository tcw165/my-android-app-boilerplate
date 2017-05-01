cmake_minimum_required(VERSION 3.4.1)

# Configure build library name.
set(TARGET_NAME protobuf_jni)

# Configure install folder.
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../../../../lib-distribution/)

# Configure import libs.
set(LIB_DIR ${CMAKE_SOURCE_DIR}/../cppLibs)

# Make install directory.
file(MAKE_DIRECTORY ${INSTALL_DIR}/${TARGET_NAME})

# Copy out lib header file...
file(COPY "${LIB_DIR}/protobuf/include"
     DESTINATION "${INSTALL_DIR}/${TARGET_NAME}")

# Copy out the lib binary.
file(COPY "${LIB_DIR}/protobuf/lib"
     DESTINATION "${INSTALL_DIR}/${TARGET_NAME}")
