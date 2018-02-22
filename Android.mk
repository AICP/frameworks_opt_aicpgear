LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := com.aicp.gear

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v7-preference
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v14-preference

# No resources in java library
#LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
#LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/preference/res
#LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v14/preference/res
#LOCAL_AAPT_FLAGS := --auto-add-overlay
#LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.preference:android.support.v14.preference

include $(BUILD_JAVA_LIBRARY)
