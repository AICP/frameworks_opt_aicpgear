LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := AicpGear

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_JAR_EXCLUDED_FILES := none

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SHARED_ANDROID_LIBRARIES := android-support-v7-preference
LOCAL_SHARED_ANDROID_LIBRARIES += android-support-v14-preference

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)
