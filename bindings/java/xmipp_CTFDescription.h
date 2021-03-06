/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class xmipp_CTFDescription */

#ifndef _Included_xmipp_jni_CTFDescription
#define _Included_xmipp_jni_CTFDescription
#ifdef __cplusplus
extern "C" {
#endif
#undef xmipp_CTFDescription_BACKGROUND_NOISE
#define xmipp_CTFDescription_BACKGROUND_NOISE 0L
#undef xmipp_CTFDescription_ENVELOPE
#define xmipp_CTFDescription_ENVELOPE 1L
#undef xmipp_CTFDescription_PSD
#define xmipp_CTFDescription_PSD 2L
#undef xmipp_CTFDescription_CTF
#define xmipp_CTFDescription_CTF 3L
/*
 * Class:     xmipp_CTFDescription
 * Method:    create
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xmipp_jni_CTFDescription_create
  (JNIEnv *, jobject);

/*
 * Class:     xmipp_CTFDescription
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xmipp_jni_CTFDescription_destroy
  (JNIEnv *, jobject);

/*
 * Class:     xmipp_CTFDescription
 * Method:    read_
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_xmipp_jni_CTFDescription_read_1
  (JNIEnv *, jobject, jstring);

/*
 * Class:     xmipp_CTFDescription
 * Method:    getFMAX
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_xmipp_jni_CTFDescription_getFMAX
  (JNIEnv *, jobject);

/*
 * Class:     xmipp_CTFDescription
 * Method:    CTFProfile
 * Signature: (DDI)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_xmipp_jni_CTFDescription_CTFProfile
  (JNIEnv *, jobject, jdouble, jdouble, jint);

/*
 * Class:     xmipp_CTFDescription
 * Method:    CTFAverageProfile
 * Signature: (DI)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_xmipp_jni_CTFDescription_CTFAverageProfile
  (JNIEnv *, jobject, jdouble, jint);

#ifdef __cplusplus
}
#endif
#endif
