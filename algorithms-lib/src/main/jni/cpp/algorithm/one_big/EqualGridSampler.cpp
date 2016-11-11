#include <cmath>
#include <sstream>
#include <cassert>
#include <algorithm>
#include <algorithm/one_big/EqualGridSampler.hpp>

EqualGridSampler::EqualGridSampler(int layoutPolicy,
                                   int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy) {
    // DO NOTHING.
}

EqualGridSampler::~EqualGridSampler() {
    // DO NOTHING.
}

std::vector<Grid> EqualGridSampler::sample(RectF& canvas,
                                           std::vector<Photo>& photos) {
    std::vector<Grid> grids;
    size_t slotNum = photos.size();

    if (slotNum < 2) {
        if (F_CHECK(genPolicy, GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS)) {
            slotNum = 2;
        } else {
            return grids;
        }
    }

    // Equal size and maximum slots for each side is 5
    // [][][]
    // [][][]
    // [][][]
    bool isAdded = false;
    size_t maxCols = 5;
    for (size_t cols = maxCols; cols >= 1; --cols) {
        if (slotNum >= cols && slotNum % cols == 0) {
            size_t n1 = cols;
            size_t n2 = slotNum / n1;
            if (n1 <= maxCols && n2 <= maxCols) {
                std::ostringstream name;
                name << "one_big: equal size " << n1 << "x" << n2;

                // TODO: Adjust slots so that its number of slots the same
                // with the number of photos.
                // Add to the output grid list.
                Grid grid = Grid::createByColsRows(name.str(),
                                                   (size_t) n1,
                                                   (size_t) n2);
                adjustToSlotNumbers(grid, slotNum);
                if (grid.slots.size() == slotNum) {
                    grids.push_back(grid);
                }
                isAdded = true;
            }
        }
    }
    if (!isAdded) {
        long n = (long) std::sqrt((double) slotNum);
        if (std::abs(n * n - (long) slotNum) < std::abs(n * (n + 1)) - (long) slotNum) {
            std::ostringstream name;
            name << "one_big: equal size " << n << "x" << n;
            // TODO: Adjust slots so that its number of slots the same with
            // the number of photos.
            // Add to the output grid list.
            Grid grid = Grid::createByColsRows(name.str(),
                                               (size_t) n,
                                               (size_t) n);
            adjustToSlotNumbers(grid, slotNum);
            if (grid.slots.size() == slotNum) {
                grids.push_back(grid);
            }
        } else {
            std::ostringstream name1;
            name1 << "one_big: equal size " << n << "x" << (n + 1);
            std::ostringstream name2;
            name2 << "one_big: equal size " << (n + 1) << "x" << n;
            // TODO: Adjust slots so that its number of slots the same with
            // TODO: the number of photos.
            // Add to the output grid list.
            Grid grid1 = Grid::createByColsRows(name1.str(),
                                                (size_t) n,
                                                (size_t) n + 1);
            adjustToSlotNumbers(grid1, slotNum);
            if (grid1.slots.size() == slotNum) {
                grids.push_back(grid1);
            }

            Grid grid2 = Grid::createByColsRows(name2.str(),
                                                (size_t) n + 1,
                                                (size_t) n);
            adjustToSlotNumbers(grid2, slotNum);
            if (grid2.slots.size() == slotNum) {
                grids.push_back(grid2);
            }
        }
    }

    return grids;
}
