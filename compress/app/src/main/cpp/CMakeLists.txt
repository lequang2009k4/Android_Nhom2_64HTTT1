# app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.4.1)
add_library(imagecompressor SHARED imagecompressor.cpp)
find_library(log-lib log)
target_link_libraries(imagecompressor ${log-lib})