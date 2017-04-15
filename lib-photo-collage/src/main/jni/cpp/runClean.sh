#!/bin/sh

echo "Cleaning CMake generated files ..."
rm -rf CMakeFiles > /dev/null 2>&1
rm CMakeCache.txt > /dev/null 2>&1
rm CTestTestfile.cmake > /dev/null 2>&1
rm cmake_install.cmake > /dev/null 2>&1
rm build > /dev/null 2>&1
echo "Done!"
