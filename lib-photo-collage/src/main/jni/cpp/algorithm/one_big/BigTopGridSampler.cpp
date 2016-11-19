#include <cmath>
#include <sstream>
#include <cassert>
#include <algorithm>
#include <algorithm/one_big/BigTopGridSampler.hpp>

BigTopGridSampler::BigTopGridSampler(int layoutPolicy,
                                     int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy) {
    // DO NOTHING.
}

BigTopGridSampler::~BigTopGridSampler() {
    // DO NOTHING.
}

std::vector<Grid> BigTopGridSampler::sample(RectF& canvas,
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

    // [      ] 0.375
    // [][][][] 0.625
    // [][][][]
    Grid grid("one_big: big top");

    uint n = (uint) std::floor(std::sqrt((double) slotNum));

    // Top slot
    RectSlot topSlot(0, 0, 1, 0.375);
    grid.slots.push_back(topSlot);

    // Bottom slots
    float width = 1.f / n;
    float height = (1.f - topSlot.height) / n;
    uint cols = n;
    uint rows = n;

    for (uint row = 0; row < rows; ++row) {
        for (uint col = 0; col < cols; ++col) {
            grid.slots.push_back(RectSlot(col * width,
                                          topSlot.height + row * height,
                                          width,
                                          height));
        }
    }

    // Add to the output grid list.
    adjustToSlotNumbers(grid, slotNum);
    if (grid.slots.size() == slotNum) {
        grids.push_back(grid);
    }

    return grids;
}
