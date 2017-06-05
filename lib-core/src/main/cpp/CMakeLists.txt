cmake_minimum_required(VERSION 3.4.1)

# Configure build library name.
set(TARGET_NAME my_core_jni)

# Configure install folder.
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../../../../lib-distribution/)

# Configure install folder for:
# 1) Finding the dependent libraries.
# 2) Copy out the generated files for others to use.
set(LIB_DIR ${CMAKE_SOURCE_DIR}/../cppLibs)

# Build application's shared lib.
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -std=c++11")

add_library(${TARGET_NAME} SHARED
            jni.cpp
            profiler.cpp)

target_include_directories(${TARGET_NAME} PRIVATE
                           ${CMAKE_SOURCE_DIR}/
                           ${CMAKE_SOURCE_DIR}/include)

target_link_libraries(${TARGET_NAME}
                      android
                      log)

# Make install directory.
file(MAKE_DIRECTORY ${INSTALL_DIR}/${TARGET_NAME})
file(MAKE_DIRECTORY ${INSTALL_DIR}/${TARGET_NAME}/include)
file(MAKE_DIRECTORY ${INSTALL_DIR}/${TARGET_NAME}/lib)

# Copy out project header file...
file(COPY "${CMAKE_SOURCE_DIR}/include"
     DESTINATION "${INSTALL_DIR}/${TARGET_NAME}/")

# Copy out the lib binary.
# FIXME: ExternalNativeBuild breaks on copying STL. It might be fixed in the
# FIXME: next Android Studio release (2.5.x).
# FIXME: Issue thread: https://github.com/android-ndk/ndk/issues/334
add_custom_command(TARGET ${TARGET_NAME} POST_BUILD

                   # Copy generated *.so files.
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy_directory "${CMAKE_SOURCE_DIR}/../../../build/intermediates/cmake/${CMAKE_BUILD_TYPE}/obj/${ANDROID_ABI}"
                   "${INSTALL_DIR}/${TARGET_NAME}/lib/${ANDROID_ABI}"

                   COMMENT "Copying ${TARGET_NAME} shared library to output directory")
#set_target_properties(${TARGET_NAME}
#                      PROPERTIES
#                      LIBRARY_OUTPUT_DIRECTORY
#                      "${INSTALL_DIR}/${TARGET_NAME}/lib/${ANDROID_ABI}")
