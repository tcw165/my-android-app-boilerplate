#ifndef CB_ALGORITHMS_LIB_DEVSIGNER_COMMON_H
#define CB_ALGORITHMS_LIB_DEVSIGNER_COMMON_H

#include <Grid.hpp>

//TODO: Remove following typedefs in the future
typedef struct {
    int rows;
    int cols;
} RowCol;

typedef struct {
    int brows;
    int bcols;
    int rrows;
    int rcols;
} FrameRule3Matrix;

typedef struct {
    RectF rect1;
    RectF rect2;
} DoubleRectF;

DoubleRectF splitRectEqualityVertically(RectF rect);

DoubleRectF splitRectEqualityHorizontally(RectF rect);

void adjustToSlotNumbers(Grid& grid,
                         size_t numberOfSlots);

#endif //CB_ALGORITHMS_LIB_DEVSIGNER_COMMON_H
