#include <string>

#include <Catch.hpp>
#include <protocol/ProtoUtil.hpp>

using namespace ::com::my::algorithm::proto;

const std::string gStrWhatever = "whatever";

inline void printPhotoId(std::vector<Photo>& photos) {
    for (int i = 0; i < photos.size(); ++i) {
        std::cout << "#" << i
                  << " Photo(" << photos[i].id << ")"
                  << std::endl;
    }
}

TEST_CASE("Protobuf") {
    SECTION("From PbPhoto to Photo:") {
        MsgPhoto pbPhoto;
        pbPhoto.set_id(1);
        pbPhoto.set_width(400);
        pbPhoto.set_height(300);

        Photo data = Photo().fromProto(pbPhoto);
        REQUIRE(data.id == 1);
        REQUIRE(data.width == 400);
        REQUIRE(data.height == 300);
    }

    SECTION("From Photo to MsgPhoto:") {
        Photo data(1, 100, 200);

        // Convert to protobuf.
        MsgPhoto pbPhoto;
        data.toProto(&pbPhoto);

        REQUIRE(pbPhoto.id() == 1);
        REQUIRE(pbPhoto.width() == 100);
        REQUIRE(pbPhoto.height() == 200);
    }

    SECTION("From MsgRectF to RectF:") {
        MsgRectF proto;
        proto.set_x(1);
        proto.set_y(2);
        proto.set_width(400);
        proto.set_height(300);

        RectF data = RectF().fromProto(proto);
        REQUIRE(data.x == 1);
        REQUIRE(data.y == 2);
        REQUIRE(data.width == 400);
        REQUIRE(data.height == 300);
    }

    SECTION("From MsgRectSlot to Slot:") {
        MsgRectSlot proto;
        proto.set_x(1);
        proto.set_y(2);
        proto.set_width(400);
        proto.set_height(300);
        proto.set_relatedphotoid(3);

        RectSlot data = dynamic_cast<RectSlot&>(RectSlot().fromProto(proto));
        REQUIRE(data.x == 1);
        REQUIRE(data.y == 2);
        REQUIRE(data.width == 400);
        REQUIRE(data.height == 300);
        REQUIRE(data.relatedPhotoId == 3);
    }

    SECTION("From MsgGrid to Grid:") {
        MsgGrid proto;
        proto.set_id(1);
        proto.set_name(gStrWhatever);

        for (int i = 0; i < 3; ++i) {
            MsgRectSlot* item = proto.add_slots();
            item->set_x(1 * (i + 1));
            item->set_y(2 * (i + 1));
            item->set_width(100 * (i + 1));
            item->set_height(100 * (i + 1));
            item->set_relatedphotoid(3u * (i + 1));
        }

        Grid data = Grid().fromProto(proto);
        REQUIRE(data.id == 1);
        REQUIRE(data.name == gStrWhatever);
        REQUIRE(data.slots.size() == proto.slots_size());

        for (int j = 0; j < proto.slots_size(); ++j) {
            REQUIRE(data.slots[j].x == 1 * (j + 1));
            REQUIRE(data.slots[j].y == 2 * (j + 1));
            REQUIRE(data.slots[j].width == 100 * (j + 1));
            REQUIRE(data.slots[j].height == 100 * (j + 1));
            REQUIRE(data.slots[j].relatedPhotoId == 3u * (j + 1));
        }
    }

    SECTION("From Grid to MsgGrid:") {
        Grid data;
        data.id = 1;
        data.name = gStrWhatever;

        for (int i = 0; i < 3; ++i) {
            RectSlot slot(2 * (i + 1),
                          3 * (i + 1),
                          100 * (i + 1),
                          100 * (i + 1),
                          4u * (i + 1));
            data.slots.push_back(slot);
        }

        // Convert to protobuf.
        MsgGrid proto;
        data.toProto(&proto);

        REQUIRE(proto.id() == 1);
        REQUIRE(proto.name() == gStrWhatever);

        for (int j = 0; j < 3; ++j) {
            REQUIRE(proto.slots(j).x() == 2 * (j + 1));
            REQUIRE(proto.slots(j).y() == 3 * (j + 1));
            REQUIRE(proto.slots(j).width() == 100 * (j + 1));
            REQUIRE(proto.slots(j).height() == 100 * (j + 1));
            REQUIRE(proto.slots(j).relatedphotoid() == 4u * (j + 1));
        }
    }

//    SECTION("From MsgPhotoList to std::vector<Photo>, the IDs should be ascending order.") {
//        MsgPhotoList msgPhotos;
//
//        for (int i = 0; i < 6; ++i) {
//            MsgPhoto* addedMsgPhoto = msgPhotos.add_items();
//
//            // Meant to assign an arbitrary wrong ID to the photo.
//            addedMsgPhoto->set_id(999);
//            addedMsgPhoto->set_width(100.f * (i + 1));
//            addedMsgPhoto->set_height(200.f * (i + 1));
//        }
//
//        std::vector<Photo> photos;
//        ProtoUtil::fromMsgPhotoList(msgPhotos, photos);
//
//        printPhotoId(photos);
//
//        for (int j = 0; j < photos.size(); ++j) {
//            REQUIRE(j == photos[j].id);
//        }
//    }
}
