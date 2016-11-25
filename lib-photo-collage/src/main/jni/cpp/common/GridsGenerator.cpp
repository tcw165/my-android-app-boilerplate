#if USE_PROTOBUF
#include <protocol/ProtoUtil.hpp>
#endif

#include <Util.hpp>
#include <GridsOptimizer.hpp>
#include <GridsGenerator.hpp>
#include <algorithm/designer/DesignerSampler.hpp>
#include <algorithm/one_big/EqualGridSampler.hpp>
#include <algorithm/one_big/BigCenterGridSampler.hpp>
#include <algorithm/one_big/BigLeftTopGridSampler.hpp>
#include <algorithm/one_big/BigTopGridSampler.hpp>

GridsGenerator::GridsGenerator(int useWhatAlgorithms,
                               int genPolicy) {
    if (F_CHECK(useWhatAlgorithms, ALGO_DESIGNER_SAMPLER)) {
        mSubGenGroup1.push_back(new DesignerSampler(LAYOUT_SCALE_TO_FILL,
                                                    genPolicy));
    }

    if (F_CHECK(useWhatAlgorithms, ALGO_EQUAL_GRID_SAMPLER)) {
        mSubGenGroup1.push_back(new EqualGridSampler(LAYOUT_SCALE_TO_FILL,
                                                     genPolicy));
    }

    if (F_CHECK(useWhatAlgorithms, ALGO_BIG_CENTER_GRID_SAMPLER)) {
        mSubGenGroup2.push_back(new BigCenterGridSampler(LAYOUT_SCALE_TO_FILL,
                                                         genPolicy));
    }

    if (F_CHECK(useWhatAlgorithms, ALGO_BIG_LEFT_TOP_GRID_SAMPLER)) {
        mSubGenGroup2.push_back(new BigLeftTopGridSampler(LAYOUT_SCALE_TO_FILL,
                                                          genPolicy));
    }

    if (F_CHECK(useWhatAlgorithms, ALGO_BIG_TOP_GRID_SAMPLER)) {
        mSubGenGroup2.push_back(new BigTopGridSampler(LAYOUT_SCALE_TO_FILL,
                                                      genPolicy));
    }

    if (F_CHECK(useWhatAlgorithms, ALGO_PIC_WALL_SAMPLER)) {
        mSubGenGroup3.push_back(new PicWallSampler(LAYOUT_SCALE_TO_FILL,
                                                   genPolicy));
    }

//    if (F_CHECK(useWhatAlgorithms, ALGO_PACK_GRID_DP_SAMPLER)) {
//        mSubGenGroup3.push_back();
//    }
}

GridsGenerator::~GridsGenerator() {
    // Free all the sub-generators.
    for (std::vector<IGridsGenerator*>::iterator i = mSubGenGroup1.begin();
         i != mSubGenGroup1.end(); ++i) {
        IGridsGenerator* subGen = *i;

        delete subGen;
    }
    for (std::vector<IGridsGenerator*>::iterator i = mSubGenGroup2.begin();
         i != mSubGenGroup2.end(); ++i) {
        IGridsGenerator* subGen = *i;

        delete subGen;
    }
    for (std::vector<IGridsGenerator*>::iterator i = mSubGenGroup3.begin();
         i != mSubGenGroup3.end(); ++i) {
        IGridsGenerator* subGen = *i;

        delete subGen;
    }

    mSubGenGroup1.clear();
    mSubGenGroup2.clear();
    mSubGenGroup3.clear();
}

std::vector<Grid> GridsGenerator::generate(RectF& canvas,
                                           std::vector<Photo>& photos,
                                           int useWhatAlgorithms,
                                           int genPolicy) {
    // The grid list.
    std::vector<Grid> grids;

    // Make sure the id is assigned in ascending order.
    for (int i = 0; i < photos.size(); ++i) {
        photos[i].id = static_cast<u_long>(i);
    }

    if (photos.size() == 0) {
        // We provide special grid options, given 0 photos.
        std::vector<Photo> photos2(2);
        std::vector<Photo> photos3(3);
        std::vector<Photo> photos4(4);
        std::vector<Photo> photos5(5);
        std::vector<Photo> photos6(6);
        std::vector<Photo> photos8(8);
        std::vector<Photo> photos9(9);

        // Shared and temporary gird list.
        std::vector<Grid> tmpGrids;

        EqualGridSampler equalGridSampler(LAYOUT_SCALE_TO_FILL, genPolicy);
        BigCenterGridSampler bigCenterGridSampler(LAYOUT_SCALE_TO_FILL, genPolicy);
        BigLeftTopGridSampler bigLeftTopGridSampler(LAYOUT_SCALE_TO_FILL, genPolicy);

        tmpGrids = equalGridSampler.sample(canvas, photos2);
        Util::append(grids, tmpGrids);

        tmpGrids = equalGridSampler.sample(canvas, photos3);
        Util::append(grids, tmpGrids);

        tmpGrids = equalGridSampler.sample(canvas, photos4);
        Util::append(grids, tmpGrids);

        tmpGrids = bigCenterGridSampler.sample(canvas, photos5);
        Util::append(grids, tmpGrids);

        tmpGrids = equalGridSampler.sample(canvas, photos6);
        Util::append(grids, tmpGrids);

        tmpGrids = bigLeftTopGridSampler.sample(canvas, photos8);
        Util::append(grids, tmpGrids);

        tmpGrids = equalGridSampler.sample(canvas, photos9);
        Util::append(grids, tmpGrids);

        tmpGrids = bigCenterGridSampler.sample(canvas, photos9);
        Util::append(grids, tmpGrids);
    } else {
        // Use local generator and it will be freed later.
        GridsGenerator gen(useWhatAlgorithms, genPolicy);

        // Go through the group 1.
        for (int i = 0; i < gen.mSubGenGroup1.size(); ++i) {
            IGridsGenerator* subGen = gen.mSubGenGroup1[i];
            // Note: The algorithm impl should NEVER alter the order of the photo list.
            // NOTE: If have to, please copy the list and then sort it.
            std::vector<Grid> subGrids = subGen->sample(canvas, photos);

            // Sort the grids.
            GridsOptimizer::sort(subGrids, canvas, photos);

            // Concatenate the two vectors.
            Util::append(grids, subGrids);
        }

        // Go through the group 2.
        for (int i = 0; i < gen.mSubGenGroup2.size(); ++i) {
            IGridsGenerator* subGen = gen.mSubGenGroup2[i];
            // Note: The algorithm impl should NEVER alter the order of the photo list.
            // NOTE: If have to, please copy the list and then sort it.
            std::vector<Grid> subGrids = subGen->sample(canvas, photos);

            // Sort the grids.
            GridsOptimizer::sort(subGrids, canvas, photos);

            // Concatenate the two vectors.
            Util::append(grids, subGrids);
        }

        // Go through the group 3.
        for (int i = 0; i < gen.mSubGenGroup3.size(); ++i) {
            IGridsGenerator* subGen = gen.mSubGenGroup3[i];
            // Note: The algorithm impl should NEVER alter the order of the photo list.
            // NOTE: If have to, please copy the list and then sort it.
            std::vector<Grid> subGrids = subGen->sample(canvas, photos);

            // Sort the grids.
            GridsOptimizer::sort(subGrids, canvas, photos);

            // Concatenate the two vectors.
            Util::append(grids, subGrids);
        }

        // Filter out the grid with similar layouts.
        GridsOptimizer::filterStructurallyEqualGrids(grids);
    }

    return grids;
}

#if USE_PROTOBUF
MsgGridList GridsGenerator::generate(MsgRectF& msgCanvas,
                                     MsgPhotoList& msgPhotos,
                                     int useWhatAlgorithms,
                                     int genPolicy) {
    // From protobuf.
    RectF canvas = RectF().fromProto(msgCanvas);
    std::vector<Photo> photos;
    ProtoUtil::fromMsgPhotoList(msgPhotos, photos);

    // Generate the grids.
    std::vector<Grid> grids = generate(canvas,
                                       photos,
                                       useWhatAlgorithms,
                                       genPolicy);

    // To protobuf.
    MsgGridList ret;
    ProtoUtil::toMsgGridList(grids, ret);

    return ret;
}
#endif
