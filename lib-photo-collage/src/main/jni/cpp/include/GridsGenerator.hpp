#ifndef CB_ALGORITHMS_LIB_GRIDS_GENERATOR_HPP
#define CB_ALGORITHMS_LIB_GRIDS_GENERATOR_HPP

#include <climits>
#include <vector>
#include <Math.hpp>
#include <IGridsGenerator.hpp>

#if USE_PROTOBUF

#include <protocol/ProtoRectF.pb.h>
#include <protocol/ProtoPhotoList.pb.h>
#include <protocol/ProtoGridList.pb.h>
// Namespace for the protobuf message clazz.
using namespace ::com::my::algorithm;
#endif

// To use what algorithms.
#define ALGO_DESIGNER_SAMPLER           0x16080001
#define ALGO_PIC_WALL_SAMPLER           0x16080002
#define ALGO_PACK_GRID_DP_SAMPLER       0x16080004
#define ALGO_EQUAL_GRID_SAMPLER         0x16080008
#define ALGO_BIG_CENTER_GRID_SAMPLER    0x16080010
#define ALGO_BIG_TOP_GRID_SAMPLER       0x16080020
#define ALGO_BIG_LEFT_TOP_GRID_SAMPLER  0x16080040

/**
 * Usage:
 * MsgGridList grids = GridsGenerator::generate(canvas, photos);
 */
class GridsGenerator {
public:
    /**
     * Generate a sample of grid list in terms of the given canvas size and
     * photos.
     *
     * @param [in] canvas               The canvas size.
     * @param [in] photos               The photo list.
     * @param [in] useWhatAlgorithms    A flag determining what algorithms to
     *                                  be used.
     */
    static std::vector<Grid> generate(RectF& canvas,
                                      std::vector<Photo>& photos,
                                      int useWhatAlgorithms,
                                      int genPolicy);

#if USE_PROTOBUF

    /**
     * Generate a sample of grid list in terms of the given canvas size and
     * photos.
     *
     * @param [in] msgCanvas            The canvas size.
     * @param [in] msgPhotos            The photo list.
     * @param [in] useWhatAlgorithms    A flag determining what algorithms to
     *                                  be used.
     */
    static MsgGridList generate(MsgRectF& msgCanvas,
                                MsgPhotoList& msgPhotos,
                                int useWhatAlgorithms,
                                int genPolicy);

#endif

protected:
    /**
     * The sub-generator pointers. Here we store data in pointers so that sub-
     * generators don't need to implement "operator=" overloading.
     */
    std::vector<IGridsGenerator*> mSubGenGroup1;
    std::vector<IGridsGenerator*> mSubGenGroup2;
    std::vector<IGridsGenerator*> mSubGenGroup3;

    /**
     * The constructor is responsible for searching available grid samplers.
     */
    GridsGenerator(int useWhatAlgorithms,
                   int genPolicy);

    /**
     * The destructor is responsible for releasing resource used by the grid samplers.
     */
    virtual ~GridsGenerator();
};


#endif //CB_ALGORITHMS_LIB_GRIDS_GENERATOR_HPP
