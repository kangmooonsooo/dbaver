/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_jkiss_wmi_service_WMIService */

#ifndef _Included_org_jkiss_wmi_service_WMIService
#define _Included_org_jkiss_wmi_service_WMIService
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    connect
 * Signature: (Lorg/apache/commons/logging/Log;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/jkiss/wmi/service/WMIService;
 */
JNIEXPORT jobject JNICALL Java_org_jkiss_wmi_service_WMIService_connect
  (JNIEnv *, jclass, jobject, jstring, jstring, jstring, jstring, jstring, jstring);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    openNamespace
 * Signature: (Ljava/lang/String;)Lorg/jkiss/wmi/service/WMIService;
 */
JNIEXPORT jobject JNICALL Java_org_jkiss_wmi_service_WMIService_openNamespace
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    executeQuery
 * Signature: (Ljava/lang/String;Lorg/jkiss/wmi/service/WMIObjectSink;J)V
 */
JNIEXPORT void JNICALL Java_org_jkiss_wmi_service_WMIService_executeQuery
  (JNIEnv *, jobject, jstring, jobject, jlong);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    enumClasses
 * Signature: (Ljava/lang/String;Lorg/jkiss/wmi/service/WMIObjectSink;J)V
 */
JNIEXPORT void JNICALL Java_org_jkiss_wmi_service_WMIService_enumClasses
  (JNIEnv *, jobject, jstring, jobject, jlong);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    enumInstances
 * Signature: (Ljava/lang/String;Lorg/jkiss/wmi/service/WMIObjectSink;J)V
 */
JNIEXPORT void JNICALL Java_org_jkiss_wmi_service_WMIService_enumInstances
  (JNIEnv *, jobject, jstring, jobject, jlong);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    cancelSink
 * Signature: (Lorg/jkiss/wmi/service/WMIObjectSink;)V
 */
JNIEXPORT void JNICALL Java_org_jkiss_wmi_service_WMIService_cancelSink
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_jkiss_wmi_service_WMIService
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jkiss_wmi_service_WMIService_close
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
