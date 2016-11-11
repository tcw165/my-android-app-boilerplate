#ifndef CB_ALGORITHMS_LIB_IPROTOPARCELABLE_H
#define CB_ALGORITHMS_LIB_IPROTOPARCELABLE_H

#if USE_PROTOBUF
#include <google/protobuf/message_lite.h>
#endif

/**
 * The abstract class that could be either serialized to a protobuf instance or
 * deserialized from a protobuf instance.
 */
template<class T>
class IProtoParcelable {
public:
#if USE_PROTOBUF
    /**
     * Instinstiate from the given protobuf instance.
     */
    virtual T& fromProto(const ::google::protobuf::MessageLite& p) = 0;

    /**
     * Convert to a protobuf instance.
     */
    virtual void toProto(::google::protobuf::MessageLite* const p) = 0;
#endif
};

#endif //CB_ALGORITHMS_LIB_IPROTOPARCELABLE_H
