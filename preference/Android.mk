LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_USE_AAPT2 := true

LOCAL_MODULE := AicpGear-preference

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_JAR_EXCLUDED_FILES := none

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SHARED_ANDROID_LIBRARIES := \
    androidx.core_core \
    androidx.preference_preference \
    androidx.appcompat_appcompat \
    androidx.recyclerview_recyclerview \
    AicpGear-util \

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)
