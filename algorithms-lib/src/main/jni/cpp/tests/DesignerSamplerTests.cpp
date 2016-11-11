#include <iostream>

#include <Catch.hpp>
#include <algorithm/designer/DesignerSampler.hpp>

TEST_CASE("DesignerSampler") {
    RectF canvas(1024.f, 1024.f);

    // 3:2 photo.
    Photo photo0(0, 1.5f);

    // 19:10 photo.
    Photo photo1(1, 1.9f);

    // 3:4 photo.
    Photo photo2(2, .75f);

    // 9:16 Photo.
    Photo photo3(3, .56f);

    // Square Photo.
    Photo photo4(4, 1.f);

    // 16:9 Photo.
    Photo photo5(5, 1.7f);

    // Some real photo.
    Photo photo6(6, 1.33333334f);

//    // Some real photo.
//    Photo photo7(7, 0.9788972f);
//
//    // Some real photo.
//    Photo photo8(8, 0.9917355f);
//
//    // Some real photo.
//    Photo photo9(9, 0.89669424f);
//
//    // Some real photo.
//    Photo photo10(10, 1.4444444f);
//
//    // Some real photo reporting from CIC.
//    Photo photo11(11, 1000.f / 562);
//
//    // Some real photo reporting from CIC.
//    Photo photo12(12, 796.f / 1024);
//
//    // Some real photo reporting from CIC.
//    Photo photo13(13, 1132.f / 1024);
//
//    // Some real photo reporting from CIC.
//    Photo photo14(14, 1280.f / 720);
//
//    // Some real photo reporting from CIC.
//    Photo photo15(15, 1280.f / 853);
//
//    // Some real photo reporting from CIC.
//    Photo photo16(16, 1280.f / 854);
//
//    // Some real photo reporting from CIC.
//    Photo photo17(17, 1280.f / 768);
//
//    // Some real photo reporting from CIC.
//    Photo photo18(1018, 622.f / 1024);

    SECTION("Take 2 photos, and put it into a canvas with 1.f.") {
        std::vector<Photo> photos;

        photos.push_back(photo0);
        photos.push_back(photo1);

        DesignerSampler sampler(LAYOUT_SCALE_TO_FILL,
                                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

//        for (int i = 0; i < grids.size(); ++i) {
//            for (int j = 0; j < grids[i].slots.size(); ++j) {
//                std::cout << grids[i].slots[j].width
//                          << " "
//                          << grids[i].slots[j].height
//                          << std::endl;
//            }
//        }

        REQUIRE(grids.size());
    }

    SECTION("Take 7 photos, and put it into a canvas with 1.f.") {
        std::vector<Photo> photos;

        photos.push_back(photo0);
        photos.push_back(photo1);
        photos.push_back(photo2);
        photos.push_back(photo3);
        photos.push_back(photo4);
        photos.push_back(photo5);
        photos.push_back(photo6);

        DesignerSampler sampler(LAYOUT_SCALE_TO_FILL,
                                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

//        for (int i = 0; i < grids.size(); ++i) {
//            for (int j = 0; j < grids[i].slots.size(); ++j) {
//                std::cout << grids[i].slots[j].width
//                          << " "
//                          << grids[i].slots[j].height
//                          << std::endl;
//            }
//        }

        REQUIRE(grids.size());
    }
}
