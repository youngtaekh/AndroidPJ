LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := Signaling
LOCAL_SRC_FILES := test.cpp
LOCAL_LDLIBS := -latomic -llog -landroid -lOpenSLES

include $(BUILD_SHARED_LIBRARY)