cmake_minimum_required(VERSION 3.4.1)

# Configure build library name.
set(TARGET_NAME dlib_jni)

# Configure install folder for:
# 1) Finding the dependent libraries.
# 2) Copy out the generated files for others to use.
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../../../../lib-distribution/)

# Configure import libs.
set(LIB_DIR ${CMAKE_SOURCE_DIR}/../cppLibs)

# Link to libdlib.so.
# Shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(dlib SHARED IMPORTED)
set_target_properties(dlib PROPERTIES IMPORTED_LOCATION
    ${LIB_DIR}/dlib/lib/${ANDROID_ABI}/libdlib.so)

# Link to libprotobuf-lite.so.
add_library(protobuf SHARED IMPORTED)
set_target_properties(protobuf PROPERTIES IMPORTED_LOCATION
    ${INSTALL_DIR}/protobuf_jni/lib/${ANDROID_ABI}/libprotobuf-lite-3.2.0.so)

# Link to libmy_core_jni.so.
add_library(my_core_jni SHARED IMPORTED)
set_target_properties(my_core_jni PROPERTIES IMPORTED_LOCATION
    ${INSTALL_DIR}/my_core_jni/lib/${ANDROID_ABI}/libmy_core_jni.so)

# Build project shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -std=c++11")
add_library(${TARGET_NAME} SHARED
            dlib-face-landmarks-detector-jni.cpp
            include/my/dlib/data/messages.pb.cc)
target_include_directories(${TARGET_NAME} PRIVATE
                           ${INSTALL_DIR}/my_core_jni/include
                           ${INSTALL_DIR}/protobuf_jni/include
                           ${CMAKE_SOURCE_DIR}/include
                           ${LIB_DIR}/dlib/include)
target_link_libraries(${TARGET_NAME}
                      dlib
                      my_core_jni
                      protobuf
                      android
                      jnigraphics
                      log)

# Make install directory.
file(MAKE_DIRECTORY ${INSTALL_DIR}/${TARGET_NAME})

# Copy out lib header file.
file(COPY "${LIB_DIR}/dlib/include/"
     DESTINATION "${INSTALL_DIR}/${TARGET_NAME}/include/")

# Copy generated protobuf message header file.
file(COPY "${CMAKE_SOURCE_DIR}/include/my/dlib/data/messages.pb.h"
     DESTINATION "${INSTALL_DIR}/${TARGET_NAME}/include/my/dlib/data/")

# Copy out the project binary.
add_custom_command(TARGET ${TARGET_NAME} POST_BUILD

                   # Copy pre-built *.so files.
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy_directory "${LIB_DIR}/dlib/lib/${ANDROID_ABI}"
                   "${INSTALL_DIR}/${TARGET_NAME}/lib/${ANDROID_ABI}"

                   # Copy generated *.so files.
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy_directory "${CMAKE_SOURCE_DIR}/../../../build/intermediates/cmake/${CMAKE_BUILD_TYPE}/obj/${ANDROID_ABI}"
                   "${INSTALL_DIR}/${TARGET_NAME}/lib/${ANDROID_ABI}"

                   COMMENT "Copying ${TARGET_NAME} shared library to output directory")
