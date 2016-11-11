#include <jni.h>
#include <android/log.h>

#include <GridsGenerator.hpp>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_DEBUG, "jni", __VA_ARGS__))

using namespace ::com::my::algorithm::proto;

extern "C" {
///////////////////////////////////////////////////////////////////////////////
// JNI Start //////////////////////////////////////////////////////////////////

JNIEXPORT jbyteArray JNICALL
Java_com_my_algorithm_GridsGenerator_generateNative(JNIEnv* env,
                                                    jobject thiz,
                                                    jbyteArray byteCanvas,
                                                    jbyteArray bytePhotos,
                                                    jint useWhatAlgorithms,
                                                    jint genPolicy) {
    try {
        jbyte* pCanvas = env->GetByteArrayElements(byteCanvas, NULL);
        jsize pCanvasLen = env->GetArrayLength(byteCanvas);
        LOGI("Receive a byte[] representing a canvas with length = %d.", pCanvasLen);

        jbyte* pPhotos = env->GetByteArrayElements(bytePhotos, NULL);
        jsize pPhotosLen = env->GetArrayLength(bytePhotos);
        LOGI("Receive a byte[] representing a photo list with length = %d.", pPhotosLen);

        // Deserialize to proto instance.
        MsgRectF canvas;
        canvas.ParseFromArray(pCanvas, pCanvasLen);
        LOGI("Receive a canvas with size of %fx%f.", canvas.width(), canvas.height());

        // Deserialize to proto instance.
        MsgPhotoList photoList;
        photoList.ParseFromArray(pPhotos, pPhotosLen);
        LOGI("Receive %d photos.", photoList.items_size());

        // Call generation function.
        MsgGridList gridList = GridsGenerator::generate(canvas,
                                                        photoList,
                                                        static_cast<int>(useWhatAlgorithms),
                                                        static_cast<int>(genPolicy));

        // Allocate a memory for serializing the result.
        int outSize = gridList.ByteSize();
        jbyte* temp = new jbyte[gridList.ByteSize()];
        gridList.SerializeToArray(temp, outSize);
        LOGI("generate %d grids.", gridList.items_size());
        for (int i = 0; i < gridList.items_size(); ++i) {
            LOGI("#%2d grid is \"%s\"", i, gridList.items(i).name().c_str());
        }

        // New return Java byte[].
        jbyteArray out = env->NewByteArray(outSize);
        env->SetByteArrayRegion(out, 0, outSize, temp);

        // Release resource.
        env->ReleaseByteArrayElements(byteCanvas, pCanvas, 0);
        env->ReleaseByteArrayElements(bytePhotos, pPhotos, 0);
        delete[] temp;

        return out;
    } catch (::google::protobuf::FatalException ex) {
        LOGI("error: %s", ex.message().c_str());
        return NULL;
    }
}

///////////////////////////////////////////////////////////////////////////////
// JNI End ////////////////////////////////////////////////////////////////////
}

