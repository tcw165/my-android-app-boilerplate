#include <cmath>
#include <sstream>
#include <cassert>
#include <algorithm>
#include <algorithm/one_big/common.hpp>

DoubleRectF splitRectEqualityVertically(RectF rect) {
    DoubleRectF doubleRect = {
            RectF(rect.left(),
                  rect.top(),
                  rect.width / 2,
                  rect.height),
            RectF(rect.left() + rect.width / 2,
                  rect.top(),
                  rect.width / 2,
                  rect.height)
    };
    return doubleRect;
}

DoubleRectF splitRectEqualityHorizontally(RectF rect) {
    DoubleRectF doubleRect = {
            RectF(rect.left(),
                  rect.top(),
                  rect.width,
                  rect.height / 2),
            RectF(rect.left(),
                  rect.top() + rect.height / 2,
                  rect.width,
                  rect.height / 2)
    };
    return doubleRect;
}

void adjustToSlotNumbers(Grid& grid,
                         size_t numberOfSlots) {
    uint t = (uint) std::abs((int) grid.slots.size() - (int) numberOfSlots);
    if (t == 0) {
        return;
    }

    if (grid.slots.size() > numberOfSlots) {
        std::vector<RectSlot> mergedSlots;

        for (long i = 0; i < t; i++) {
            RectSlot lastSlot = grid.slots.back();

            for (std::vector<RectSlot>::iterator it = grid.slots.begin();
                 it != grid.slots.end(); ++it) {
                if (lastSlot.top() == it->top()
                    && lastSlot.left() == it->right()
                    && lastSlot.height == it->height) {

                    // Merge left and right
                    //  ________________
                    // |         |      |
                    // | current | last |
                    // |_________|______|

                    RectSlot mergedSlot = RectSlot(it->left(), it->top(),
                                                   it->width + lastSlot.width,
                                                   it->height);
                    mergedSlots.push_back(mergedSlot);

                    // Remove last and current
                    // Add them back as as a mergedSlot later
                    grid.slots.pop_back();
                    grid.slots.erase(it);
                    break;
                } else if (lastSlot.left() == it->left()
                           && lastSlot.top() == it->bottom()
                           && lastSlot.width == it->width) {

                    // Merge top and bottom
                    //  _________
                    // | current |
                    // |---------|
                    // |  last   |
                    // |---------|

                    RectSlot mergedSlot = RectSlot(it->left(), it->top(), it->width, it->height + it->height);
                    mergedSlots.push_back(mergedSlot);

                    grid.slots.pop_back();
                    grid.slots.erase(it);
                    break;
                }
            }
        }

        for (long i = 0; i < mergedSlots.size(); ++i) {
            grid.slots.push_back(mergedSlots[i]);
        }
    } else {
        int splitStartIndex = std::max(0, (int) grid.slots.size() - (int) t);
        std::vector<RectSlot> newSlots = std::vector<RectSlot>();

        for (std::vector<RectSlot>::iterator it = grid.slots.begin() + splitStartIndex;
             it != grid.slots.end(); ++it) {

            DoubleRectF doubleRect = it->width > it->height ?
                                     splitRectEqualityVertically((RectF) *it) :
                                     splitRectEqualityHorizontally((RectF) *it);

            RectF rect1 = doubleRect.rect1;
            it->setRect(rect1.left(), rect1.top(), rect1.width, rect1.height);

            RectF rect2 = doubleRect.rect2;
            RectSlot newSlot = RectSlot(rect2.left(), rect2.top(), rect2.width, rect2.height);

            newSlots.push_back(newSlot);
        }

        for (long i = 0; i < newSlots.size(); ++i) {
            grid.slots.push_back(newSlots[i]);
        }
    }
    // TODO: focusTop fails on case numberOfSlots 49
    assert(grid.slots.size() == numberOfSlots);
}
