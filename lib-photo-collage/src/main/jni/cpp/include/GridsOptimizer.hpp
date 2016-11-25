#ifndef CB_ALGORITHMS_LIB_GRIDS_OPTIMIZER_HPP
#define CB_ALGORITHMS_LIB_GRIDS_OPTIMIZER_HPP

#include <vector>
#include <Photo.hpp>
#include <Grid.hpp>
#if USE_PROTOBUF
#include <protocol/ProtoGridList.pb.h>
#include <protocol/ProtoPhotoList.pb.h>
#endif

class GridsOptimizer {
public:
    /**
     * Sort the given frame list in a order that most fits the given photo
     * list. The order change will be apply directly to the given frame list.
     *
     * @param [in,out]  targetGrids     The grids list to be sorted.
     * @param [in]      canvas          The canvas size.
     * @param [in]      photos          The reference photos list.
     */
    static void sort(std::vector<Grid>& targetGrids,
                     RectF& canvas,
                     std::vector<Photo>& photos);

    /**
     * Filter out thoes grid options with similar layout.
     *
     * @param [in,out]  targetGrids     The slots list to be tidied up.
     */
    static void filterStructurallyEqualGrids(std::vector<Grid>& targetGrids);

    /**
     * Tell if the given two grids are structurally equal.
     *
     * @param [in]      a               The first grid.
     * @param [in]      b               The second grid.
     */
    static bool isTwoGridStructurallyEqual(Grid& a,
                                           Grid& b);
};

#endif //CB_ALGORITHMS_LIB_GRIDS_OPTIMIZER_HPP
