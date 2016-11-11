#include <iostream>

#include <Catch.hpp>
#include <algorithm/one_big/EqualGridSampler.hpp>
#include <algorithm/one_big/BigCenterGridSampler.hpp>
#include <algorithm/one_big/BigLeftTopGridSampler.hpp>
#include <algorithm/one_big/BigTopGridSampler.hpp>

TEST_CASE("OneBigSampler") {
    RectF canvas(1024.f, 1024.f);

    SECTION("Test EqualGridSampler, given 6 photos.") {
        std::vector<Photo> photos(6);

        EqualGridSampler sampler(LAYOUT_SCALE_TO_FILL,
                                 GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

        REQUIRE(grids.size());

        for (int i = 0; i < grids.size(); ++i) {
            INFO(grids[i].name);
            REQUIRE(grids[i].slots.size() >= 6);
        }
    }

    SECTION("Test BigCenterGridSampler, given 9 photos.") {
        std::vector<Photo> photos(9);

        BigCenterGridSampler sampler(LAYOUT_SCALE_TO_FILL,
                                     GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

        REQUIRE(grids.size());

        for (int i = 0; i < grids.size(); ++i) {
            INFO(grids[i].name);
            REQUIRE(grids[i].slots.size() >= 9);
        }
    }

    SECTION("Test BigLeftTopGridSampler, given 9 photos.") {
        std::vector<Photo> photos(9);

        BigLeftTopGridSampler sampler(LAYOUT_SCALE_TO_FILL,
                                      GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

        REQUIRE(grids.size());

        for (int i = 0; i < grids.size(); ++i) {
            INFO(grids[i].name);
            REQUIRE(grids[i].slots.size() >= 9);
        }
    }

    SECTION("Test BigLeftTopGridSampler, given 9 photos.") {
        std::vector<Photo> photos(7);

        BigTopGridSampler sampler(LAYOUT_SCALE_TO_FILL,
                                  GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        std::vector<Grid> grids = sampler.sample(canvas, photos);

        REQUIRE(grids.size());

        for (int i = 0; i < grids.size(); ++i) {
            INFO(grids[i].name);
            REQUIRE(grids[i].slots.size() >= 7);
        }
    }
}
