cmake_minimum_required(VERSION 3.4.1)

# Configure import libs
set(lib_dir ${CMAKE_SOURCE_DIR}/../cppLibs)
message(STATUS "lib_dir=${lib_dir}")

# Link to libdlib.so.
# Shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(dlib SHARED IMPORTED)
set_target_properties(dlib PROPERTIES IMPORTED_LOCATION
    ${lib_dir}/dlib/lib/${ANDROID_ABI}/libdlib.so)

# Link to libprotobuf-lite.so.
add_library(protobuf SHARED IMPORTED)
set_target_properties(protobuf PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../../../../lib-protobuf/cppLibs/lib/${ANDROID_ABI}/libprotobuf-lite-3.2.0.so)

# Build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -std=c++11")

add_library(dlib_jni SHARED
            dlib-jni.cpp
            data/messages.pb.cc)

target_include_directories(dlib_jni PRIVATE
                           ${CMAKE_SOURCE_DIR}
                           ${CMAKE_SOURCE_DIR}/../../../../lib-protobuf/cppLibs/include
                           ${lib_dir}/dlib/include)

target_link_libraries(dlib_jni
                      dlib
                      protobuf
                      android
                      log)
