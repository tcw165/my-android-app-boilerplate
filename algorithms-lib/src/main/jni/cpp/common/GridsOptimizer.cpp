#include <cmath>
#include <vector>
#include <iostream>
#include <algorithm>
#if USE_PROTOBUF
#include <protocol/ProtoUtil.hpp>
#endif
#include <GridsOptimizer.hpp>

#define GRID_STRUCTURALLY_EQUAL_EPSILON 0.3f

///////////////////////////////////////////////////////////////////////////
// Static Clazz ///////////////////////////////////////////////////////////

/**
 * The {@code Grid} wrapper for sorting in certain order.
 */
class GridWrap {
public:
    /**
     * Copy of the real grid.
     */
    Grid realGrid;
    float penalty;

    /**
     * The {@code Grid} wrapper for sorting in certain order.
     *
     * @param grid The grid.
     * @param shapeVar The shape variance from {@code calcShapeVariance}
     * @param angelVar The diagonal variance from
     * {@code calcDiagonalAngleVariance}
     */
    GridWrap(Grid grid,
             const float shapeVar,
             const float angleVar,
             const float slotVar)
            : realGrid(grid) {
        float c1 = 1;
        float c2 = 1;
        float c3 = 1;
        penalty = c1 * shapeVar + c2 * angleVar + c3 * slotVar;
    }

    GridWrap& operator=(const GridWrap& grid) {
        realGrid = grid.realGrid;
        penalty = grid.penalty;

        return *this;
    }

    friend std::ostream& operator<<(std::ostream& output,
                                    const GridWrap& data) {
        if (data.realGrid.id == IGNORED_GRID_ID) {
            output << "GridWrap(\"" << data.realGrid.name << "\",\n"
                   << "  id=ignored" << ",\n"
                   << "  size=" << data.realGrid.slots.size() << ",\n"
                   << "  penalty=" << data.penalty << ",\n"
                   << ")";
        } else {
            output << "GridWrap(\"" << data.realGrid.name << "\",\n"
                   << "  id=" << data.realGrid.id << ",\n"
                   << "  size=" << data.realGrid.slots.size() << ",\n"
                   << "  penalty=" << data.penalty << ",\n"
                   << ")";
        }
        return output;
    }
};

///////////////////////////////////////////////////////////////////////////
// Static Methods /////////////////////////////////////////////////////////

bool penaltySorter(GridWrap a,
                   GridWrap b) {
    return a.penalty < b.penalty;
}

bool coordAndAreaSorter(RectSlot a,
                        RectSlot b) {
    return a.x <= b.x &&
           a.y <= b.y &&
           a.width <= b.width &&
           a.height <= b.height;
}

//bool comparePhotoAspectRatioInOrder(Photo a,
//                                    Photo b) {
//    return a.aspectRatio < b.aspectRatio;
//}

//void sortPhotosByAspectRatioInOrder(std::vector<Photo>& photos) {
//    std::sort(photos.begin(), photos.end(), comparePhotoAspectRatioInOrder);
//}

bool compareSlotAspectRatioInOrder(RectSlot a,
                                   RectSlot b) {
    return a.aspectRatio < b.aspectRatio;
}

void sortSlotsByAspectRatioInOrder(std::vector<RectSlot>& slots) {
    std::sort(slots.begin(), slots.end(), compareSlotAspectRatioInOrder);
}

void sortAllSlotsInGrids(std::vector<Grid>& grids) {
    for (int i = 0; i < grids.size(); ++i) {
        sortSlotsByAspectRatioInOrder(grids[i].slots);
    }
}

float calcShapeVariance(Grid& grid,
                        std::vector<Photo>& photos) {
    float variance = 0;
    size_t size = std::min(grid.slots.size(), photos.size());

    for (size_t i = 0; i < size; ++i) {
        RectSlot& slot = grid.slots[i];

        if (slot.relatedPhotoId < photos.size()) {
            Photo& photo = photos[slot.relatedPhotoId];

            // Calc the variance score.
            variance += std::max(slot.aspectRatio / photo.aspectRatio,
                                 photo.aspectRatio / slot.aspectRatio);
        }
    }

    return variance;
}

float calcDiagonalAngleVariance(Grid& grid,
                                std::vector<Photo>& photos) {
    float variance = 0;
    // tan(photo diagonal tan), tan(realGrid slot diagonal).
    float pTan, sTan;
    size_t size = std::min(grid.slots.size(), photos.size());

    for (size_t i = 0; i < size; ++i) {
        RectSlot& slot = grid.slots[i];

        if (slot.relatedPhotoId < photos.size()) {
            pTan = photos[slot.relatedPhotoId].aspectRatioRecip;
            sTan = grid.slots[i].aspectRatioRecip;

            // Calc the diagonal angle variance.
            variance += std::abs(std::atan(pTan) - std::atan(sTan));
        }
    }

    return variance;
}

float calcSlotAreaVariance(RectF& canvas,
                           Grid& grid,
                           std::vector<Photo>& photos) {
    float variance = 0;
    float canvasSize = canvas.width * canvas.height;
    float size = std::min(grid.slots.size(), photos.size());
    float threshold = canvasSize * (1.f - 1.f / size) * 0.5f / size;

    for (size_t i = 0; i < size; ++i) {
        float area = grid.slots[i].width * grid.slots[i].height;
        if (area < threshold) {
            variance += (threshold - area) / canvasSize;
        }
    }

    return variance;
}

///////////////////////////////////////////////////////////////////////////////

void GridsOptimizer::sort(std::vector<Grid>& targetGrids,
                          RectF& canvas,
                          std::vector<Photo>& photos) {
//    // FIXME: Since the every slots has a linkage to the related photo, do
//    // FIXME: we still need to sort them all???
//
//    // Make sure the given lists is sorted.
//    sortPhotosByAspectRatioInOrder(photos);
    sortAllSlotsInGrids(targetGrids);

    // Calc the penalty.
    std::vector<GridWrap> gridWraps;
    for (int i = 0; i < targetGrids.size(); ++i) {
        Grid& grid = targetGrids[i];
        gridWraps.push_back(GridWrap(grid,
                                     calcShapeVariance(grid, photos),
                                     calcDiagonalAngleVariance(grid, photos),
                                     calcSlotAreaVariance(canvas, grid, photos)));
    }

    // Sort the realGrid-extras list in terms of its penalty.
    std::sort(gridWraps.begin(), gridWraps.end(), penaltySorter);

    // Compose a new grids list as the return.
    targetGrids.clear();
    for (int i = 0; i < gridWraps.size(); ++i) {
        targetGrids.push_back(gridWraps[i].realGrid);
    }

    // Log.
#if defined(NDEBUG) || defined(DEBUG)
    for (int k = 0; k < targetGrids.size(); ++k) {
        std::cout << "=== generated grids ===" << std::endl;
        std::cout << targetGrids[k]<< std::endl;
    }
#endif
}

void GridsOptimizer::filterStructurallyEqualGrids(std::vector<Grid>& targetGrids) {
    // Sort the slots in x-y-area order.
    for (int i = 0; i < targetGrids.size(); ++i) {
        Grid& grid = targetGrids[i];
        std::sort(grid.slots.begin(), grid.slots.end(), coordAndAreaSorter);
    }

    std::vector<Grid>::iterator it1 = targetGrids.begin();

    while (it1 != targetGrids.end()) {
        std::vector<Grid>::iterator it2 = it1 + 1;

        while (it2 != targetGrids.end()) {
            if (it1->slots.size() == it2->slots.size() &&
                isTwoGridStructurallyEqual(*it1, *it2)) {
                it2 = targetGrids.erase(it2);
            } else {
                ++it2;
            }
        }

        ++it1;
    }
}

bool GridsOptimizer::isTwoGridStructurallyEqual(Grid& a,
                                                Grid& b) {
    bool isEqual = true;
    for (int i = 0; i < a.slots.size(); ++i) {
        RectSlot& slot1 = a.slots[i];
        RectSlot& slot2 = b.slots[i];
        if (!Math::approximatelyEqual(slot1.x, slot2.x, GRID_STRUCTURALLY_EQUAL_EPSILON) ||
            !Math::approximatelyEqual(slot1.y, slot2.y, GRID_STRUCTURALLY_EQUAL_EPSILON) ||
            !Math::approximatelyEqual(slot1.width, slot2.width, GRID_STRUCTURALLY_EQUAL_EPSILON) ||
            !Math::approximatelyEqual(slot1.height, slot2.height, GRID_STRUCTURALLY_EQUAL_EPSILON)) {
            isEqual = false;
            break;
        }
    }
    return isEqual;
}
