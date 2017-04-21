cmake_minimum_required(VERSION 3.4.1)

# Configure import libs
set(distribution_dir ${CMAKE_SOURCE_DIR}/../cppLibs)
message(STATUS "distribution_dir=${distribution_dir}")

# Shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(lib_dlib SHARED IMPORTED)
set_target_properties(lib_dlib PROPERTIES IMPORTED_LOCATION
    ${distribution_dir}/dlib/lib/${ANDROID_ABI}/libdlib.so)

# Build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library(hello-libs SHARED
            hello-libs.cpp)

target_include_directories(hello-libs PRIVATE
                           ${distribution_dir}/dlib/include)

target_link_libraries(hello-libs
                      lib_dlib
                      android
                      log)
