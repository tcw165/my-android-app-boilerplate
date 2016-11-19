#include <iostream>
#include <Catch.hpp>
#include <Math.hpp>
#include <GridsOptimizer.hpp>

using namespace std;

inline std::string printGrids(std::vector<Grid>& grids) {
    std::ostringstream msg;

    msg << std::endl;
    for (int i = 0; i < grids.size(); ++i) {
        msg << "#" << i << " Grid:"
            << std::endl;
        for (int j = 0; j < grids[i].slots.size(); ++j) {
            msg << "- " << " slot " << j << ":"
                << std::endl;
            msg << "    aspect ratio = " << grids[i].slots[j].aspectRatio
                << std::endl;
        }
    }

    return msg.str();
}

TEST_CASE("GridsOptimizer") {
    SECTION("Sorting 6 grids with 3 out of 6 are similar and 2 out of 6 are similar.") {
        vector<Photo> photos;
        u_long photoId = 0;

        photos.push_back(Photo(photoId++, 200, 200));
        photos.push_back(Photo(photoId++, 200, 200));
        photos.push_back(Photo(photoId++, 200, 200));
        photos.push_back(Photo(photoId++, 200, 200));
        photos.push_back(Photo(photoId++, 200, 200));
        photos.push_back(Photo(photoId, 200, 200));

        vector<Grid> grids;

        // [][]
        // [][]
        // [][]
        grids.push_back(Grid("test 01"));
        grids[0].slots.push_back(RectSlot(0,    0,     0.5f, 0.32f));
        grids[0].slots.push_back(RectSlot(0,    0.32f, 0.5f, 0.33f));
        grids[0].slots.push_back(RectSlot(0,    0.65f, 0.5f, 0.35f));
        grids[0].slots.push_back(RectSlot(0.5f, 0,     0.5f, 0.35f));
        grids[0].slots.push_back(RectSlot(0.5f, 0.35f, 0.5f, 0.29f));
        grids[0].slots.push_back(RectSlot(0.5f, 0.64f, 0.5f, 0.36f));

        // [][][]
        // [][][]
        grids.push_back(Grid("test 02"));
        grids[1].slots.push_back(RectSlot(0,     0,    0.33f, 0.5f));
        grids[1].slots.push_back(RectSlot(0.33f, 0,    0.33f, 0.5f));
        grids[1].slots.push_back(RectSlot(0.66f, 0,    0.34f, 0.5f));
        grids[1].slots.push_back(RectSlot(0,     0.5f, 0.33f, 0.5f));
        grids[1].slots.push_back(RectSlot(0.33f, 0.5f, 0.33f, 0.5f));
        grids[1].slots.push_back(RectSlot(0.66f, 0.5f, 0.34f, 0.5f));

        // [][]
        // [][]
        // [][]
        grids.push_back(Grid("test 03"));
        grids[2].slots.push_back(RectSlot(0, 0, 0.5f, 0.33f));
        grids[2].slots.push_back(RectSlot(0, 0.33f, 0.5f, 0.33f));
        grids[2].slots.push_back(RectSlot(0, 0.66f, 0.5f, 0.34f));
        grids[2].slots.push_back(RectSlot(0.5f, 0, 0.5f, 0.33f));
        grids[2].slots.push_back(RectSlot(0.5f, 0.33f, 0.5f, 0.33f));
        grids[2].slots.push_back(RectSlot(0.5f, 0.66f, 0.5f, 0.34f));

        // [][][]
        // [][][]
        grids.push_back(Grid("test 04"));
        grids[3].slots.push_back(RectSlot(0,     0,    0.33f, 0.5f));
        grids[3].slots.push_back(RectSlot(0.33f, 0,    0.33f, 0.5f));
        grids[3].slots.push_back(RectSlot(0.66f, 0,    0.34f, 0.5f));
        grids[3].slots.push_back(RectSlot(0,     0.5f, 0.33f, 0.5f));
        grids[3].slots.push_back(RectSlot(0.33f, 0.5f, 0.33f, 0.5f));
        grids[3].slots.push_back(RectSlot(0.66f, 0.5f, 0.34f, 0.5f));

        // [][]
        // [][]
        // [][]
        grids.push_back(Grid("test 05"));
        grids[4].slots.push_back(RectSlot(0, 0, 0.5f, 0.33f));
        grids[4].slots.push_back(RectSlot(0, 0.33f, 0.5f, 0.33f));
        grids[4].slots.push_back(RectSlot(0, 0.66f, 0.5f, 0.34f));
        grids[4].slots.push_back(RectSlot(0.5f, 0, 0.5f, 0.33f));
        grids[4].slots.push_back(RectSlot(0.5f, 0.33f, 0.5f, 0.33f));
        grids[4].slots.push_back(RectSlot(0.5f, 0.66f, 0.5f, 0.34f));

        RectF canvas(960.f, 640.f);

        GridsOptimizer::sort(grids, canvas, photos);
        GridsOptimizer::filterStructurallyEqualGrids(grids);

        // Print it out.
        WARN(printGrids(grids));

        for (int i = 0; i < grids.size(); ++i) {
            Grid& grid1 = grids[i];

            for (int j = i + 1; j < grids.size(); ++j) {
                Grid& grid2 = grids[j];

                REQUIRE(!GridsOptimizer::isTwoGridStructurallyEqual(grid1, grid2));
            }
        }
    }
}
