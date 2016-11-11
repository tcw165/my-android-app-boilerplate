cmake_minimum_required(VERSION 2.8)

project(UnitTest)

# Define ${PROJECT_DIR}.
get_filename_component(PROJECT_DIR "${CMAKE_CURRENT_SOURCE_DIR}" ABSOLUTE)

# Enable debug symbols.
set(CMAKE_BUILD_TYPE Debug)

# CXX Flags.
# Enable runtime-type-identification and exceptions.
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -frtti")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -fexceptions")
# Enable protobuf by default and the wrapper/bridge can disable it optionally.
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -DUSE_PROTOBUF=1")
# Enable debugging.
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -DDEBUG")
# The protobuf takes this flag.
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -DHAVE_PTHREAD=0")
if (USE_CPP11)
    ## We can't turn this on by default, since it breaks on travis
    message(STATUS "Enabling C++11")
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -std=c++11 ")
endif ()

# enable @rpath in the install name for any shared library being built
# note: it is planned that a future version of CMake will enable this by default
set(CMAKE_MACOSX_RPATH 1)

###############################################################################
# *.h #########################################################################

include_directories(
        ${PROJECT_DIR}
        ${PROJECT_DIR}/include
        ${PROJECT_DIR}/tests
        # 3rd party headers.
        ${PROJECT_DIR}/third_party
)

###############################################################################
# tests/*.cpp #################################################################

file(GLOB_RECURSE TEST_SOURCES
        ${PROJECT_DIR}/tests/*.hpp
        ${PROJECT_DIR}/tests/*.cpp
        )

###############################################################################
# */*.c(pp|c) #################################################################
file(GLOB_RECURSE SOURCES
        ${PROJECT_DIR}/include/*.hpp
        ${PROJECT_DIR}/include/*.cpp
        ${PROJECT_DIR}/common/*.hpp
        ${PROJECT_DIR}/common/*.cpp
        ${PROJECT_DIR}/protocol/*.h
        ${PROJECT_DIR}/protocol/*.cc
        ${PROJECT_DIR}/algorithm/*.hpp
        ${PROJECT_DIR}/algorithm/*.cpp
        )

set(SOURCES_LIB_PROTOBUF_LITE
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/atomicops_internals_x86_gcc.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/atomicops_internals_x86_msvc.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/bytestream.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/bytestream.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/common.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/hash.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/int128.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/int128.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/map_util.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/mathutil.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/once.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/shared_ptr.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/status.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/status.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/status_macros.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/statusor.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/statusor.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/stringpiece.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/stringpiece.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/stringprintf.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/stringprintf.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/structurally_valid.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/strutil.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/strutil.h
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/time.cc
        ${PROJECT_DIR}/third_party/google/protobuf/stubs/time.h
        ${PROJECT_DIR}/third_party/google/protobuf/arena.cc
        ${PROJECT_DIR}/third_party/google/protobuf/arenastring.cc
        ${PROJECT_DIR}/third_party/google/protobuf/extension_set.cc
        ${PROJECT_DIR}/third_party/google/protobuf/generated_message_util.cc
        ${PROJECT_DIR}/third_party/google/protobuf/message_lite.cc
        ${PROJECT_DIR}/third_party/google/protobuf/repeated_field.cc
        ${PROJECT_DIR}/third_party/google/protobuf/wire_format_lite.cc
        ${PROJECT_DIR}/third_party/google/protobuf/io/coded_stream.cc
        ${PROJECT_DIR}/third_party/google/protobuf/io/coded_stream_inl.h
        ${PROJECT_DIR}/third_party/google/protobuf/io/zero_copy_stream.cc
        ${PROJECT_DIR}/third_party/google/protobuf/io/zero_copy_stream_impl_lite.cc
        )

list(REMOVE_ITEM SOURCES ${TEST_SOURCES})
list(SORT SOURCES)

message(STATUS "SOURCE = ${SOURCES}")
message(STATUS "TEST_SOURCES = ${TEST_SOURCES}")

###############################################################################
# UnitTest Exe ################################################################

# Build libprotobuf_lite_static.a and libprotobuf_lite.so.
add_library(protobuf_lite_static
        STATIC
        ${SOURCES_LIB_PROTOBUF_LITE})
add_library(protobuf_lite
        SHARED
        ${SOURCES_LIB_PROTOBUF_LITE})

# Build UnitTest exe.
add_executable(UnitTest
        ${SOURCES}
        ${TEST_SOURCES})

# Link UnitTest exe with libprotobuf_lite.a.
target_link_libraries(UnitTest
        protobuf_lite_static)
