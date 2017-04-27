// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

#include <jni.h>
#include <android/log.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/image_io.h>
#include <data/messages.pb.h>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "dlib-jni:", __VA_ARGS__))

#define JNI_METHOD(NAME) \
    Java_com_my_jni_dlib_FaceLandmarksDetector_##NAME

using namespace ::com::my::jni::dlib::data;

dlib::shape_predictor sShapePredictor;
dlib::frontal_face_detector sFaceDetector;

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(deserializeShapeDetector)(JNIEnv *env,
                                     jobject thiz,
                                     jstring detectorPath) {
    const char *path = env->GetStringUTFChars(detectorPath, JNI_FALSE);

    // We need a shape_predictor. This is the tool that will predict face
    // landmark positions given an image and face bounding box.  Here we are just
    // loading the model from the shape_predictor_68_face_landmarks.dat file you gave
    // as a command line argument.
    // Deserialize the shape detector.
    dlib::deserialize(path) >> sShapePredictor;
    LOGI("L%d: shape predictor is initialized", __LINE__);

    env->ReleaseStringUTFChars(detectorPath, path);
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(deserializeFaceDetector)(JNIEnv *env,
                                    jobject thiz) {
    sFaceDetector = dlib::get_frontal_face_detector();
    LOGI("L%d: face detector is initialized", __LINE__);
}

//// TODO: Implement a Java wrapper for it.
extern "C" JNIEXPORT jbyteArray JNICALL
JNI_METHOD(findFaces)(JNIEnv *env,
                      jobject thiz,
                      jstring imgPath) {
    // Convert bitmap to a list of rgb_pixel.
    const char *path = env->GetStringUTFChars(imgPath, JNI_FALSE);
    dlib::array2d<dlib::rgb_pixel> img;
    dlib::load_image(img, path);
    env->ReleaseStringUTFChars(imgPath, path);
    LOGI("L%d: input image (w=%lu, h=%lu) is read.", __LINE__, img.nc(), img.nr());

//    // Make the image larger so we can detect small faces.
//    dlib::pyramid_up(img);
//    LOGI("L%d: pyramid_up the input image (w=%lu, h=%lu).", __LINE__, img.nc(), img.nr());

    // Now tell the face detector to give us a list of bounding boxes
    // around all the faces in the image.
    std::vector<dlib::rectangle> dets = sFaceDetector(img);
    LOGI("L%d: Number of faces detected: %d", __LINE__, dets.size());

    // Protobuf message.
    FaceList faces;
    // Now we will go ask the shape_predictor to tell us the pose of
    // each face we detected.
    std::vector<dlib::full_object_detection> shapes;
    for (unsigned long j = 0; j < dets.size(); ++j) {
        dlib::full_object_detection shape = sShapePredictor(img, dets[j]);
        LOGI("L%d: #%lu face, %lu of parts detected",
             __LINE__, j, shape.num_parts());
        // Protobuf message.
        Face* face = faces.add_faces();
        // You get the idea, you can get all the face part locations if
        // you want them.  Here we just store them in shapes so we can
        // put them on the screen.
        for (u_long i = 0 ; i < shape.num_parts(); ++i) {
            dlib::point& pt = shape.part(i);

            LOGI("L%d: point #%lu (x=%lu,y=%lu)",
                 __LINE__, i, pt.x(), pt.y());
            Landmark* landmark = face->add_landmarks();
            landmark->set_x((::google::protobuf::uint32) pt.x());
            landmark->set_y((::google::protobuf::uint32) pt.y());
        }

        shapes.push_back(shape);
    }

    // Prepare the return message.
    int outSize = faces.ByteSize();
    jbyteArray out = env->NewByteArray(outSize);
    jbyte* buffer = new jbyte[outSize];

    faces.SerializeToArray(buffer, outSize);
    env->SetByteArrayRegion(out, 0, outSize, buffer);
    delete[] buffer;

    return out;
}
