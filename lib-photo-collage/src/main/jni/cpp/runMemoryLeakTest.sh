#!/usr/bin/env sh

# Clean the CMake generated files
source runClean.sh

# Generate codes.
source runCompileProto.sh

cmake . && make clean && make && \
valgrind \
--leak-check=summary \
--show-leak-kinds=definite,indirect,possible \
--track-origins=yes \
./UnitTest