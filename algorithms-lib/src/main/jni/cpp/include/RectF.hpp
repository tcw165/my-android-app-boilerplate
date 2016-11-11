#ifndef CB_ALGORITHMS_LIB_RECTF_H
#define CB_ALGORITHMS_LIB_RECTF_H

#include <Math.hpp>
#include <IProtoParcelable.hpp>

#if USE_PROTOBUF
#include <protocol/ProtoRectF.pb.h>

using namespace ::com::my::algorithm::proto;
#endif

class RectF : public IProtoParcelable<RectF> {
public:
    float x;
    float y;
    float width;
    float height;
    float aspectRatio;
    float aspectRatioRecip;

    RectF() : x(0.f),
              y(0.f),
              width(0.f),
              height(0.f),
              aspectRatio(0.f),
              aspectRatioRecip(0.f) {}

    RectF(float width,
          float height)
            : x(0),
              y(0),
              width(width),
              height(height) {
        updateAspectRatio();
    }

    RectF(float x,
          float y,
          float width,
          float height)
            : x(x),
              y(y),
              width(width),
              height(height),
              aspectRatio(0.f),
              aspectRatioRecip(0.f) {
        updateAspectRatio();
    }

#if USE_PROTOBUF
    /**
     * Instinstiate from the given protobuf {@code MsgRectF} instance.
     */
    virtual RectF& fromProto(const ::google::protobuf::MessageLite& p) {
        const MsgRectF& proto = dynamic_cast<const MsgRectF&>(p);

        x = proto.x();
        y = proto.y();
        width = proto.width();
        height = proto.height();

        updateAspectRatio();

        return *this;
    }
    /**
     * Convert to a protobuf {@code MsgRectF} instance.
     */
    virtual void toProto(::google::protobuf::MessageLite* const p) {
        MsgRectF* proto = dynamic_cast<MsgRectF*>(p);

        proto->set_x(x);
        proto->set_y(y);
        proto->set_width(width);
        proto->set_height(height);
    }
#endif

    RectF& operator=(const RectF& rect) {
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
        aspectRatio = rect.aspectRatio;
        aspectRatioRecip = rect.aspectRatioRecip;

        return *this;
    }

    float left() { return x; }

    float top() { return y; }

    float right() { return x + width; }

    float bottom() { return y + height; }

    void setRect(float x,
                 float y,
                 float width,
                 float height) {
        this->x = x;
        this->y = y;
        this->width = width;
        this->height = height;

        updateAspectRatio();
    }

    void setNormalizedRect(float x,
                           float y,
                           float width,
                           float height,
                           float totalWidth,
                           float totalHeight) {
        setRect(x, y, width, height);

        this->x /= totalWidth;
        this->width /= totalWidth;
        this->y /= totalHeight;
        this->height /= totalHeight;

        updateAspectRatio();
    }

protected:
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
                aspectRatioRecip = 0.f;
            } else {
                aspectRatioRecip = 1.f / aspectRatio;
            }
        }
    }
};

#endif //CB_ALGORITHMS_LIB_RECTF_H
