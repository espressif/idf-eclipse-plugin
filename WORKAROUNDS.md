# Workarounds for build errors
## Clang Toolchain buid errors
1. ``error: `__cxa_guard_release(abort)` is missing exception specification `throw()``. Edit a file `esp-idf/components/cxx/cxx_guards.cpp`

```diff
-extern "C" void __cxa_guard_release(__guard* pg)
+extern "C" void __cxa_guard_release(__guard* pg) throw()
 {
     guard_t* g = reinterpret_cast<guard_t*>(pg);
     const auto scheduler_started = xTaskGetSchedulerState() != taskSCHEDULER_NOT_STARTED;
     ...
 }
 
-extern "C" void __cxa_guard_abort(__guard* pg)
+extern "C" void __cxa_guard_abort(__guard* pg) throw()
 {
     guard_t* g = reinterpret_cast<guard_t*>(pg);
     const auto scheduler_started = xTaskGetSchedulerState() != taskSCHEDULER_NOT_STARTED;
```

2. `error: variable 'usec' set but not used [-Werror,-Wunused-but-set-variable] int sec, usec`. Edit a file `esp-idf/CMakeLists.txt`.

```diff
     list(APPEND compile_options "-Wno-atomic-alignment")
     # Clang also produces many -Wunused-function warnings which GCC doesn't.
     # However these aren't treated as errors.

+    list(APPEND compile_options "-Wno-unused-but-set-variable")
+    list(APPEND compile_options "-Wno-unused-command-line-argument")
+    list(APPEND compile_options "-Wno-unknown-warning-option")
```
3. `error: equality comparison with extraneous parentheses [-Werror,-Wparentheses-equality]`. Edit a file `esp-idf/CMakeLists.txt`.

```diff
    list(APPEND compile_options "-Wno-unused-but-set-variable")
    list(APPEND compile_options "-Wno-unused-command-line-argument")
    list(APPEND compile_options "-Wno-unknown-warning-option")
+   list(APPEND compile_options "-Wno-parentheses-equality")
endif()
```
4. Windows specific issue saying clang++ file is missing from the toolchain folder:

```
 The CMAKE_CXX_COMPILER:
clang++
  is not a full path and was not found in the PATH.
```
To fix this you can use clang instead of clang++. Edit a file `esp-idf\tools\cmake\toolchain-clang-esp32.cmake`:

```diff
set(CMAKE_C_COMPILER clang)
- set(CMAKE_CXX_COMPILER clang++)
+ set(CMAKE_CXX_COMPILER clang)
set(CMAKE_ASM_COMPILER clang)
```
