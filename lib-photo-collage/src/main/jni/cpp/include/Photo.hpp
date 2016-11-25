#ifndef CB_ALGORITHMS_LIB_PHOTO_H
#define CB_ALGORITHMS_LIB_PHOTO_H

#ifdef _WINRT_DLL
#include <ExtDefs.h>
#endif // _WINRT_DLL

#include <sys/types.h>
#include <limits.h>

#include <Math.hpp>
#include <IProtoParcelable.hpp>

#if USE_PROTOBUF
#include <protocol/ProtoPhoto.pb.h>
using namespace ::com::my::algorithm::proto;
#endif

#define INVALID_PHOTO_ID            ULONG_MAX

#define MAX_ASPECT_RATIO            3.7682f
#define MIN_ASPECT_RATIO            0.2682f


class Photo : public IProtoParcelable<Photo> {
public:
    u_long id;

    float width;
    float height;
    /**ç
     * Constrained width to prevent passing extreme landscape/portrait photo
     * size to the grid algorithms.
     */
    float constrainedWidth;
    /**
     * Constrained height to prevent passing extreme landscape/portrait photo
     * size to the grid algorithms.
     */
    float constrainedHeight;

    float aspectRatio;
    float aspectRatioRecip;
    /**
     * Constrained aspect ratio to prevent passing extreme landscape/portrait
     * photo size to the grid algorithms.
     */
    float constrainedAspectRatio;
    /**
     * Constrained reciprocal of aspect ratio to prevent passing extreme
     * landscape/portrait photo size to the grid algorithms.
     */
    float constrainedAspectRatioRecip;

    Photo() : id(INVALID_PHOTO_ID),
              width(0),
              height(0),
              aspectRatio(0.f),
              aspectRatioRecip(0.f) {}

    Photo(u_long id,
          float width,
          float height)
            : id(id),
              width(width),
              height(height) {
        updateAspectRatio();
    }

    Photo(u_long id,
          float aspectRatio)
            : id(id),
              width(aspectRatio),
              height(1.f) {
        updateAspectRatio();
    }

    void setWidth(float width) {
        this->width = width;

        updateAspectRatio();
    }

    void setHeight(float height) {
        this->height = height;

        updateAspectRatio();
    }

    void setSize(float width, float height) {
        this->width = width;
        this->height = height;

        updateAspectRatio();
    }

#if USE_PROTOBUF
    /**
     * Instinstiate from the given protobuf {@code MsgPhoto} instance.
     */
    virtual Photo& fromProto(const ::google::protobuf::MessageLite& p) {
        const MsgPhoto& proto = dynamic_cast<const MsgPhoto&>(p);

        id = proto.id();
        width = proto.width();
        height = proto.height();

        updateAspectRatio();

        return *this;
    }

    /**
     * Convert to a protobuf {@code MsgPhoto} instance.
     */
    virtual void toProto(::google::protobuf::MessageLite* const p) {
        MsgPhoto* proto = dynamic_cast<MsgPhoto*>(p);

        // Copy primitive fields.
        proto->set_id(id);
        proto->set_width(width);
        proto->set_height(height);
    }
#endif

    Photo& operator=(const Photo& other) {
        id = other.id;

        width = other.width;
        height = other.height;
        aspectRatio = other.aspectRatio;
        aspectRatioRecip = other.aspectRatioRecip;

        constrainedWidth = other.constrainedWidth;
        constrainedHeight = other.constrainedHeight;
        constrainedAspectRatio = other.constrainedAspectRatio;
        constrainedAspectRatioRecip = other.constrainedAspectRatioRecip;

        return *this;
    }

protected:

    /**
     * Update the aspect ratio in terms of the width and height.
     */
    void updateAspectRatio() {
        if (Math::approximatelyEqual(height,
                                     0.f,
                                     MATH_APPROX_EQUAL_EPSILON)) {
            aspectRatio = aspectRatioRecip = 0.f;
        } else {
            aspectRatio = width / height;

            if (Math::approximatelyEqual(aspectRatio,
                                         0.f,
                                         MATH_APPROX_EQUAL_EPSILON)) {
                aspectRatio = aspectRatioRecip = 0.f;
            } else {
                aspectRatioRecip = 1.f / aspectRatio;
            }
        }

        // Constrain the aspect ratio so that we guarantee there won't be
        // extreme landscape or portrait slot in the outcome.
        if (aspectRatio > MAX_ASPECT_RATIO) {
            constrainedWidth = width * MAX_ASPECT_RATIO;
            constrainedHeight = height;
            constrainedAspectRatio = MAX_ASPECT_RATIO;
        } else if (aspectRatio < MIN_ASPECT_RATIO) {
            constrainedWidth = width;
            constrainedHeight = height / MIN_ASPECT_RATIO;
            constrainedAspectRatio = MIN_ASPECT_RATIO;
        } else {
            constrainedWidth = width;
            constrainedHeight = height;
            constrainedAspectRatio = aspectRatio;
        }

        // Update the constrained aspect ratio reciprocal.
        if (Math::approximatelyEqual(constrainedAspectRatio,
                                     0.f,
                                     MATH_APPROX_EQUAL_EPSILON)) {
            constrainedAspectRatio = constrainedAspectRatioRecip = 0.f;
        } else {
            constrainedAspectRatioRecip = 1.f / constrainedAspectRatio;
        }
    }
};

#endif //CB_ALGORITHMS_LIB_PHOTO_H
