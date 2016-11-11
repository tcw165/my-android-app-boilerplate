#include <Catch.hpp>

#include <sstream>
#include <Photo.hpp>
#include <protocol/ProtoUtil.hpp>
#include <GridsGenerator.hpp>

int ALL_ALGO = (ALGO_DESIGNER_SAMPLER |
                ALGO_EQUAL_GRID_SAMPLER |
                ALGO_BIG_CENTER_GRID_SAMPLER |
                ALGO_BIG_LEFT_TOP_GRID_SAMPLER |
                ALGO_BIG_TOP_GRID_SAMPLER |
                ALGO_PIC_WALL_SAMPLER);

inline std::string printSlotIDs(std::vector<Grid>& grids) {
    std::ostringstream msg;

    for (int i = 0; i < grids.size(); ++i) {
        Grid& grid = grids[i];

        msg << "grid #" << i << std::endl;
        msg << "  name = \"" << grid.name << "\""
            << std::endl;
        msg << "  related photo IDs = ";
        for (int j = 0; j < grid.slots.size(); ++j) {
            RectSlot& slot = grid.slots[j];

            if (slot.relatedPhotoId == INVALID_PHOTO_ID) {
                msg << "?, ";
            } else {
                msg << slot.relatedPhotoId << ", ";
            }
        }
        msg << std::endl;
    }
    msg << std::endl;

    return msg.str();
}

TEST_CASE("GridsGenerator") {
    SECTION("Test given 0 photos.") {
        RectF canvas(1024, 1288);
        std::vector<Photo> photos(0);

        // Given 0 photos, the generate provides you the default grid options
        // no matter the usedWhatAlgorithms parameter.
        std::vector<Grid> grids = GridsGenerator::generate(
                canvas,
                photos,
                0,
                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        REQUIRE(grids.size());

        // Log.
        INFO(printSlotIDs(grids));

        // Test it again.
        grids = GridsGenerator::generate(
                canvas,
                photos,
                ALL_ALGO,
                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        REQUIRE(grids.size());
    }

    SECTION("Given 6 photos, and the related photo IDs of slots should be unique.") {
        RectF canvas(1024, 1288);
        std::vector<Photo> photos;
        photos.push_back(Photo(0, 300, 400));
        photos.push_back(Photo(1, 300, 400));
        photos.push_back(Photo(2, 300, 400));
        photos.push_back(Photo(3, 300, 400));
        photos.push_back(Photo(4, 300, 400));
        photos.push_back(Photo(5, 300, 400));

        std::vector<Grid> grids = GridsGenerator::generate(
                canvas,
                photos,
                ALL_ALGO,
                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);

        REQUIRE(grids.size());

        // Log.
        WARN(printSlotIDs(grids));

        for (int i = 0; i < grids.size(); ++i) {
            Grid& grid = grids[i];
            std::vector<size_t> check(grid.slots.size());

            for (int j = 0; j < grid.slots.size(); ++j) {
                RectSlot& slot = grid.slots[j];

                if (slot.relatedPhotoId != INVALID_PHOTO_ID) {
                    REQUIRE(slot.relatedPhotoId < photos.size());
                    ++check[slot.relatedPhotoId];
                    REQUIRE(check[slot.relatedPhotoId] <= 1);
                }
            }
        }
    }

    SECTION("Given 8 photos, and the related photo IDs of slots should be unique.") {
        RectF canvas(1024, 1288);
        std::vector<Photo> photos;
        photos.push_back(Photo(0, 1000.f, 562.f));
        photos.push_back(Photo(1, 796.f, 1024.f));
        photos.push_back(Photo(2, 1132.f, 1024.f));
        photos.push_back(Photo(3, 1280.f, 720.f));
        photos.push_back(Photo(4, 1280.f, 853.f));
        photos.push_back(Photo(5, 1280.f, 854.f));
        photos.push_back(Photo(6, 1280.f, 768.f));
        photos.push_back(Photo(7, 622.f, 1024.f));

        std::vector<Grid> grids = GridsGenerator::generate(
                canvas,
                photos,
                ALL_ALGO,
                GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);

        REQUIRE(grids.size());

        // Log.
        WARN(printSlotIDs(grids));

        for (int i = 0; i < grids.size(); ++i) {
            Grid& grid = grids[i];
            std::vector<size_t> check(grid.slots.size());

            for (int j = 0; j < grid.slots.size(); ++j) {
                RectSlot& slot = grid.slots[j];

                if (slot.relatedPhotoId != INVALID_PHOTO_ID) {
                    REQUIRE(slot.relatedPhotoId < photos.size());
                    ++check[slot.relatedPhotoId];
                    REQUIRE(check[slot.relatedPhotoId] <= 1);
                }
            }
        }
    }
}