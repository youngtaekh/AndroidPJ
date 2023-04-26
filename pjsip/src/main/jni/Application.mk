APP_OPTIM := debug
APP_ABI := arm64-v8a armeabi-v7a
APP_DEBUG = true

APP_PLATFORM := android-21

APP_STL := c++_shared
APP_CPPFLAGS := -std=c++11 -frtti -fexceptions -DDEBUG -DPOSIX
APP_CPPFLAGS += -I/Users/young/workspace/CallLibrary/signaling/src/main/jni/
APP_CPPFLAGS += -I/Users/young/workspace/openssl-1.1.0h/openssl-1.1.0h/include/
APP_CPPFLAGS += -I/Users/young/workspace/pjproject-2.10/pjlib/include/
APP_CPPFLAGS += -I/Users/young/workspace/pjproject-2.10/pjsip/include/
APP_CPPFLAGS += -I/Users/young/workspace/pjproject-2.10/pjmedia/include/

NDK_TOOLCHAIN_VERSION := 4.9

APP_BUILD_SCRIPT := Android.mk