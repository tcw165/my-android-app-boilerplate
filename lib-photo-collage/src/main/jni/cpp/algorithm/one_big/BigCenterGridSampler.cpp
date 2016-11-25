#include <cmath>
#include <sstream>
#include <cassert>
#include <algorithm>
#include <algorithm/one_big/BigCenterGridSampler.hpp>

BigCenterGridSampler::BigCenterGridSampler(int layoutPolicy,
                                           int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy) {
    // DO NOTHING.
}

BigCenterGridSampler::~BigCenterGridSampler() {
    // DO NOTHING.
}

std::vector<Grid> BigCenterGridSampler::sample(RectF& canvas,
                                               std::vector<Photo>& photos) {
    std::vector<Grid> grids;
    size_t slotNum = photos.size();

    if (slotNum < 5) {
        if (F_CHECK(genPolicy, GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS)) {
            slotNum = 5;
        } else {
            return grids;
        }
    }

    // [][][][]
    // [][  ][]
    // [][][][]

    // _________
    // | |__3___|
    // |2|    | |
    // |_|____|1|
    // |__4___|_|
    Grid grid("one_big: big center");

    // Center one, which is not dividable.
    RectSlot centerSlot(0.25, 0.25, 0.5, 0.5);

    // Slot 1, which is dividable.
    RectSlot slot1(0.75, 0.25, 0.25, 0.75);
    // Slot 2, which is dividable.
    RectSlot slot2(0, 0, 0.25, 0.75);
    // Slot 3, which is dividable.
    RectSlot slot3(0.25, 0, 0.75, 0.25);
    // Slot 4, which is dividable.
    RectSlot slot4(0, 0.75, 0.75, 0.25);

    grid.slots.push_back(centerSlot);

    // Divide the surrounding slots if necessary.
    u_int slotNumEachSide = (u_int) (slotNum - 1) / 4;
    RectSlot dividableSlots[4] = {slot1, slot2, slot3, slot4};
    for (int i = 0; i < 4; ++i) {
        RectSlot& slot = dividableSlots[i];

        // Need to be divided into smaller slots.
        if (slotNumEachSide > 1) {
            if (slot.width > slot.height) {
                float width = slot.width / slotNumEachSide;
                for (int c = 0; c < slotNumEachSide; ++c) {
                    grid.slots.push_back(RectSlot(slot.x + width * c,
                                                  slot.y,
                                                  width,
                                                  slot.height));
                }
            } else {
                float height = slot.height / slotNumEachSide;
                for (int c = 0; c < slotNumEachSide; ++c) {
                    grid.slots.push_back(RectSlot(slot.x,
                                                  slot.y + height * c,
                                                  slot.width,
                                                  height));
                }
            }
        } else {
            grid.slots.push_back(slot);
        }
    }

    // Add to the output grid list.
    adjustToSlotNumbers(grid, slotNum);
    if (grid.slots.size() == slotNum) {
        grids.push_back(grid);
    }

    return grids;
}
