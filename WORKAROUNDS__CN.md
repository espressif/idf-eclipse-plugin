# 构建错误的临时解决方法

[English](./WORKAROUNDS.md)

## Clang 工具链构建错误

1. ``error: `__cxa_guard_release(abort)` is missing exception specification `throw()``。编辑文件 `esp-idf/components/cxx/cxx_guards.cpp`

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

2. `error: variable 'usec' set but not used [-Werror,-Wunused-but-set-variable] int sec, usec`。编辑文件 `esp-idf/CMakeLists.txt`.

```diff
     list(APPEND compile_options "-Wno-atomic-alignment")
     # Clang also produces many -Wunused-function warnings which GCC doesn't.
     # However these aren't treated as errors.

+    list(APPEND compile_options "-Wno-unused-but-set-variable")
+    list(APPEND compile_options "-Wno-unused-command-line-argument")
+    list(APPEND compile_options "-Wno-unknown-warning-option")
```

3. `error: equality comparison with extraneous parentheses [-Werror,-Wparentheses-equality]`。编辑文件 `esp-idf/CMakeLists.txt`.

```diff
    list(APPEND compile_options "-Wno-unused-but-set-variable")
    list(APPEND compile_options "-Wno-unused-command-line-argument")
    list(APPEND compile_options "-Wno-unknown-warning-option")
+   list(APPEND compile_options "-Wno-parentheses-equality")
endif()
```
4. Windows 特殊问题：工具链文件夹中找不到 clang++ 文件：

```
 The CMAKE_CXX_COMPILER:
clang++
  is not a full path and was not found in the PATH.
```
可尝试使用 clang，而非 clang++。编辑文件 `esp-idf\tools\cmake\toolchain-clang-esp32.cmake`：

```diff
set(CMAKE_C_COMPILER clang)
- set(CMAKE_CXX_COMPILER clang++)
+ set(CMAKE_CXX_COMPILER clang)
set(CMAKE_ASM_COMPILER clang)
```