#include <cmath>
#include <sstream>
#include <cassert>
#include <algorithm>
#include <algorithm/one_big/BigLeftTopGridSampler.hpp>

BigLeftTopGridSampler::BigLeftTopGridSampler(int layoutPolicy,
                                             int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy) {
    // DO NOTHING.
}

BigLeftTopGridSampler::~BigLeftTopGridSampler() {
    // DO NOTHING.
}

std::vector <Grid> BigLeftTopGridSampler::sample(RectF& canvas,
                                                 std::vector <Photo>& photos) {
    std::vector <Grid> grids;
    size_t slotNum = photos.size();

    if (slotNum < 3) {
        if (F_CHECK(genPolicy, GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS)) {
            slotNum = 3;
        } else {
            return grids;
        }
    }

    // |  |[]
    // |  |[]
    // [][][]
    Grid grid("one_big: big left-top");

    int n = (int) slotNum;
    uint max = (uint) round(std::sqrt(n)) + 1;
    n = n - 1;

    RowCol bRowCol = {0, 0};
    RowCol rRowCol = {0, 0};
    while (bRowCol.cols == 0 || bRowCol.rows == 0 ||
           rRowCol.cols == 0 || rRowCol.rows == 0) {
        for (int col = max; col >= 1; --col) {
            for (int row = std::max(1, std::min(2, n / 4)); row <= 2; ++row) {
                int v = (n - row * col);
                if (v > 0) {
                    RowCol ele1 = {row, col};
                    int max2 = (int) round(std::sqrt(v)) + 1;
                    for (int col2 = max2; col2 >= 1; col2--) {
                        for (int row2 = std::max(1, std::min(2, v / 4)); row2 <= 2; row2++) {
                            int v2 = (v - row2 * col2);
                            if (v2 == 0) {
                                RowCol ele2 = {row2, col2};
                                if (ele1.cols > ele2.cols) {
                                    bRowCol = ele1;
                                    rRowCol = ele2;
                                } else {
                                    bRowCol = ele2;
                                    rRowCol = ele1;
                                }
                                goto found;
                            }
                        }
                    }
                }
            }
        }
        n = n - 1;
        found:;
    }
    if (rRowCol.rows == 1 || rRowCol.cols < rRowCol.rows) {
        int rows = rRowCol.rows;
        int cols = rRowCol.cols;
        rRowCol.rows = cols;
        rRowCol.cols = rows;
    }
    FrameRule3Matrix frameMatrix = {bRowCol.rows,
                                    bRowCol.cols,
                                    rRowCol.rows,
                                    rRowCol.cols};

    int brows = frameMatrix.brows;
    int bcols = frameMatrix.bcols;
    int rrows = frameMatrix.rrows;
    int rcols = frameMatrix.rcols;

    RectF TLSlotSize(1.f / bcols * (bcols - rcols),
                     1.f - (float) rcols / bcols);
    TLSlotSize.height = std::max(0.4f, 1.f - (float) brows * 0.25f);
    TLSlotSize.width = std::max(0.4f, 1.f / bcols * (bcols - rcols));

    RectF BslotSize(1.f / bcols, (1.f - TLSlotSize.height) / brows);
    RectF RslotSize(std::min((1 - TLSlotSize.width) / rcols, BslotSize.width * rcols),
                    (1.0f - BslotSize.height * brows) / rrows);

    u_long photoId = 0;
    grid.slots.push_back(RectSlot(0.f,
                                  0.f,
                                  TLSlotSize.width,
                                  TLSlotSize.height,
                                  photoId++));


    for (int row = 0; row < brows; row++) {
        for (int col = 0; col < bcols; col++) {
            grid.slots.push_back(RectSlot(col * BslotSize.width,
                                          TLSlotSize.height + row * BslotSize.height,
                                          BslotSize.width,
                                          BslotSize.height,
                                          photoId++));
        }
    }

    for (int row = 0; row < rrows; row++) {
        for (int col = 0; col < rcols; col++) {
            grid.slots.push_back(RectSlot(TLSlotSize.width + col * RslotSize.width,
                                          row * RslotSize.height,
                                          RslotSize.width,
                                          RslotSize.height,
                                          photoId++));
        }
    }

    // Add to the output grid list.
    adjustToSlotNumbers(grid, slotNum);
    if (grid.slots.size() == slotNum) {
        grids.push_back(grid);
    }

    return grids;
}
