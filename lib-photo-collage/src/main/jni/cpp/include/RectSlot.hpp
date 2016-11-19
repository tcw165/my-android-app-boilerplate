#ifndef CB_ALGORITHMS_LIB_RECTSLOT_H
#define CB_ALGORITHMS_LIB_RECTSLOT_H

#include <string>
#include <climits>
#include <sys/types.h>
#include <iostream>

#include <RectF.hpp>
#include <IProtoParcelable.hpp>
#if USE_PROTOBUF
#include <protocol/ProtoRectSlot.pb.h>
using namespace ::com::my::algorithm::proto;
#endif

#define INVALID_RELATED_PHOTO_ID ULONG_MAX


class RectSlot : public RectF {
public:

    u_long relatedPhotoId;

    RectSlot()
            : RectF(),
              relatedPhotoId(INVALID_RELATED_PHOTO_ID) {}

    RectSlot(float x,
             float y,
             float width,
             float height)
            : RectF(x, y, width, height),
              relatedPhotoId(INVALID_RELATED_PHOTO_ID) {}

    RectSlot(float x,
             float y,
             float width,
             float height,
             u_long relatedPhotoId)
            : RectF(x, y, width, height),
              relatedPhotoId(relatedPhotoId) {}


#if USE_PROTOBUF
    /**
     * Instinstiate from the given protobuf {@code MsgRectSlot} instance.
     */
    virtual RectF& fromProto(const ::google::protobuf::MessageLite& p) {
        const MsgRectSlot& proto = dynamic_cast<const MsgRectSlot&>(p);

        x = proto.x();
        y = proto.y();
        width = proto.width();
        height = proto.height();
        relatedPhotoId = proto.relatedphotoid();

        updateAspectRatio();

        return *this;
    }

    /**
     * Convert to a protobuf {@code MsgRectSlot} instance.
     */
    virtual void toProto(::google::protobuf::MessageLite* const p) {
        MsgRectSlot* proto = dynamic_cast<MsgRectSlot*>(p);

        proto->set_x(x);
        proto->set_y(y);
        proto->set_width(width);
        proto->set_height(height);
        proto->set_relatedphotoid(relatedPhotoId);
    }
#endif

    RectSlot& operator=(const RectSlot& other) {
        x = other.x;
        y = other.y;
        width = other.width;
        height = other.height;
        aspectRatio = other.aspectRatio;
        aspectRatioRecip = other.aspectRatioRecip;
        relatedPhotoId = other.relatedPhotoId;

        return *this;
    }

    friend std::ostream& operator<<(std::ostream& output,
                                    const RectSlot& data) {
        output << "Slot("
               << "x=" << data.x << ", "
               << "y=" << data.y << ", "
               << "width=" << data.width << ", "
               << "height=" << data.height << ", "
               << ")";

        return output;
    }
};

#endif //CB_ALGORITHMS_LIB_RECTSLOT_H
