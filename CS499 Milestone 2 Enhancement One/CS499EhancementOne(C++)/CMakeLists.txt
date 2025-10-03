cmake_minimum_required(VERSION 3.25)
project(BallPhysicsProject LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Nice defaults for MSVC warnings
if(MSVC)
  add_compile_options(/W4 /permissive-)
else()
  add_compile_options(-Wall -Wextra -Wpedantic)
endif()

include(FetchContent)

# ---- GLFW (windows/input/context) ----
FetchContent_Declare(
  glfw
  GIT_REPOSITORY https://github.com/glfw/glfw.git
  GIT_TAG 3.4   # pin a real version
)
FetchContent_MakeAvailable(glfw)   # provides target: glfw

# ---- GLM (math) ----
FetchContent_Declare(
  glm
  GIT_REPOSITORY https://github.com/g-truc/glm.git
  GIT_TAG 1.0.1
)
FetchContent_MakeAvailable(glm)    # header-only; provides target: glm

# ---- Your app ----
file(GLOB_RECURSE SRC CONFIGURE_DEPENDS
  "${CMAKE_SOURCE_DIR}/src/*.cpp"
)
add_executable(BallPhysicsProject ${SRC})
target_include_directories(BallPhysicsProject PRIVATE "${CMAKE_SOURCE_DIR}/include")

# Link GLFW + OpenGL (Windows)
target_link_libraries(BallPhysicsProject PRIVATE glfw)
if (WIN32)
  target_link_libraries(BallPhysicsProject PRIVATE opengl32)
endif()

# Use GLM headers
target_link_libraries(BallPhysicsProject PRIVATE glm)

set_target_properties(BallPhysicsProject PROPERTIES
  RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
  VS_DEBUGGER_WORKING_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)
