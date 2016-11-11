#ifndef CB_ALGORITHMS_LIB_PROTOUTIL_H
#define CB_ALGORITHMS_LIB_PROTOUTIL_H

#include <Grid.hpp>
#include <Photo.hpp>
#include <IProtoParcelable.hpp>
#include <protocol/ProtoPhotoList.pb.h>
#include <protocol/ProtoGridList.pb.h>

using namespace ::com::my::algorithm::proto;

class ProtoUtil {
public:

    /**
     * Convert {@code MsgPhotoList} type to {@code std::vector<Photo>} type.
     */
    static void fromMsgPhotoList(MsgPhotoList& fromList,
                                 std::vector<Photo>& toList) {
        for (int i = 0; i < fromList.items_size(); ++i) {
            Photo photo = Photo().fromProto(fromList.items()
                                                    .Get(i));

            // Add to list.
            toList.push_back(photo);
        }
    }

    /**
     * Convert {@code MsgGridList} type to {@code std::vector<Grid>} type.
     */
    static void fromMsgGridList(MsgGridList& fromList,
                                std::vector<Grid>& toList) {
        for (int i = 0; i < fromList.items_size(); ++i) {
            Grid grid = Grid().fromProto(fromList.items()
                                                 .Get(i));

            // Add to list.
            toList.push_back(grid);
        }
    }

    /**
     * Convert {@code std::vector<Photo>} type to {@code MsgPhotoList} type.
     */
    static void toMsgPhotoList(std::vector<Photo>& fromList,
                               MsgPhotoList& toList) {
        // Clean the old fields.
        toList.Clear();

        for (std::vector<Photo>::iterator p = fromList.begin();
             p != fromList.end(); ++p) {
            Photo& inPhoto = *p;
            // Add an item.
            MsgPhoto* outPhoto = toList.add_items();

            // Update the fields.
            outPhoto->set_id(inPhoto.id);
            outPhoto->set_width(inPhoto.width);
            outPhoto->set_height(inPhoto.height);
        }
    }

    /**
     * Convert {@code std::vector<Grid>} type to {@code MsgGridList} type.
     */
    static void toMsgGridList(std::vector<Grid>& fromList,
                              MsgGridList& toList) {
        // Clean the old fields.
        toList.Clear();

        for (std::vector<Grid>::iterator g = fromList.begin();
             g != fromList.end(); ++g) {
            Grid& inGrid = *g;
            // Add an item.
            MsgGrid* outGrid = toList.add_items();

            // Update its fields.
            outGrid->set_id(inGrid.id);
            outGrid->set_name(inGrid.name);

            // Update its slots.
            std::vector<RectSlot>& inSlots = inGrid.slots;
            for (std::vector<RectSlot>::iterator s = inSlots.begin();
                 s != inSlots.end(); ++s) {
                RectSlot& inSlot = *s;
                MsgRectSlot* outSlot = outGrid->add_slots();

                // Copy the slot.
                inSlot.toProto(outSlot);
            }
        }
    }
};

#endif //CB_ALGORITHMS_LIB_PROTOUTIL_H
