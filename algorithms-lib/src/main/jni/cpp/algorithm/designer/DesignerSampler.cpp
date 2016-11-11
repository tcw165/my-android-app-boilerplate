#include <cstdlib>
#include <string>
#include <sstream>
#include <algorithm/designer/DesignerSampler.hpp>


const std::string gGridName("designer: ");

DesignerSampler::DesignerSampler(int layoutPolicy,
                                 int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy) {
    mBucket.resize(15);

    // A temporary realGrid list container.
    u_long photoId = 0;
    std::vector<Grid> tmpGrids;

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "2x1"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 0.5f, 1.f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.f, 0.5f, 1.f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1x2"));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.5f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.5f, 1.f, 0.5f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1 small top + 1 bigger bottom"));
    tmpGrids[2].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.33f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.f, 0.33f, 1.f, 0.67f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1 bigger top + 1 small bottom"));
    tmpGrids[3].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.66f, photoId++));
    tmpGrids[3].slots.push_back(RectSlot(0.f, 0.66f, 1.f, 0.34f, photoId));

    mBucket[2] = tmpGrids;

    tmpGrids.clear();

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1x3"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.33f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.33f, 1.f, 0.34f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.67f, 1.f, 0.33f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1+2"));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.f, 0.5f, 1.f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.5f, 0.f, 0.5f, 0.5f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.5f, 0.5f, 0.5f, 0.5f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "3x1"));
    tmpGrids[2].slots.push_back(RectSlot(0.f, 0.f, 0.333331f, 1.f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.333331f, 0.f, 0.333331f, 1.f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.666662f, 0.f, 0.333331f, 1.f, photoId));

    mBucket[3] = tmpGrids;

    tmpGrids.clear();

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "2x2"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 0.5f, 0.5f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.f, 0.5f, 0.5f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.5f, 0.5f, 0.5f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.5f, 0.5f, 0.5f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1x4"));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.25f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.25f, 1.f, 0.25f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.5f, 1.f, 0.25f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.75f, 1.f, 0.25f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1 top + 3 bottom"));
    tmpGrids[2].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.33f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.f, 0.33f, 0.33f, 0.67f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.33f, 0.33f, 0.34f, 0.67f, photoId++));
    tmpGrids[2].slots.push_back(RectSlot(0.67f, 0.33f, 0.33f, 0.67f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "3 top + 1 bottom"));
    tmpGrids[3].slots.push_back(RectSlot(0.f, 0.f, 0.33f, 0.66f, photoId++));
    tmpGrids[3].slots.push_back(RectSlot(0.33f, 0.f, 0.34f, 0.66f, photoId++));
    tmpGrids[3].slots.push_back(RectSlot(0.67f, 0.f, 0.33f, 0.66f, photoId++));
    tmpGrids[3].slots.push_back(RectSlot(0.f, 0.66f, 1.f, 0.34f, photoId));

    mBucket[4] = tmpGrids;

    tmpGrids.clear();

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "1 + 2 + 2"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 1.f, 0.33f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.33f, 0.5f, 0.33f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.33f, 0.5f, 0.33f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.66f, 0.5f, 0.34f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.66f, 0.5f, 0.33f, photoId));

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "2 + 1 + 2"));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.f, 0.5f, 0.33f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.5f, 0.f, 0.5f, 0.33f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.33f, 1.f, 0.34f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.f, 0.67f, 0.5f, 0.33f, photoId++));
    tmpGrids[1].slots.push_back(RectSlot(0.5f, 0.67f, 0.5f, 0.33f, photoId));

    mBucket[5] = tmpGrids;

    tmpGrids.clear();

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "2x3"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 0.5f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.f, 0.5f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.333331f, 0.5f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.333331f, 0.5f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.666663f, 0.5f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.5f, 0.666663f, 0.5f, 0.333331f, photoId));

    mBucket[6] = tmpGrids;

    // Use Picwall to generate some default grids.
    tmpGrids.clear();
    genDefault(1, 1, 5, tmpGrids);
    genDefault(1, 2, 4, tmpGrids);
    genDefault(2, 1, 4, tmpGrids);
    genDefault(2, 2, 3, tmpGrids);

    mBucket[7] = tmpGrids;

    tmpGrids.clear();
    genDefault(1, 1, 6, tmpGrids);
    genDefault(1, 2, 5, tmpGrids);
    genDefault(2, 1, 5, tmpGrids);
    genDefault(2, 2, 4, tmpGrids);

    mBucket[8] = tmpGrids;

    tmpGrids.clear();

    photoId = 0;
    tmpGrids.push_back(Grid(gGridName + "3x3"));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.3333331f, 0.f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.666663f, 0.f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.333331f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.333331f, 0.333331f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.666663f, 0.333331f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.f, 0.666663f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.333331f, 0.666663f, 0.333331f, 0.333331f, photoId++));
    tmpGrids[0].slots.push_back(RectSlot(0.666663f, 0.666663f, 0.333331f, 0.333331f, photoId));

    mBucket[9] = tmpGrids;
}

DesignerSampler::~DesignerSampler() {
    mBucket.clear();
}

std::vector<Grid> DesignerSampler::sample(RectF& canvas,
                                          std::vector<Photo>& photos) {
    if (photos.size() < mBucket.size() && photos.size() > 0) {
        return mBucket[photos.size()];
    } else {
        // TODO: We still need valid results.
        return std::vector<Grid>(0);
    }
}

void DesignerSampler::genDefault(size_t ptNum,
                                 size_t lsNum,
                                 size_t sqNum,
                                 std::vector<Grid>& outGrids) {
    std::vector<Photo> photos;

    for (size_t i = 0; i < ptNum; ++i) {
        photos.push_back(Photo(i, 0.5f));
    }

    for (size_t i = 0; i < lsNum; ++i) {
        photos.push_back(Photo(ptNum + i, 2.f));
    }

    for (size_t i = 0; i < sqNum; ++i) {
        photos.push_back(Photo(ptNum + lsNum + i, 1.f));
    }

    PicWallSampler sampler(layoutPolicy, genPolicy);

    // Maximum trials according to the number of photos.
    u_long maxTrials = photos.size();

    RectF normCanvas(1.f, 1.f);
    std::vector<Grid> newGrid;
    while (maxTrials > 0) {
        // We use pic-wall algorithm to generate grids here, but the pic-wall
        // algorithm takes longer to calculate. In order to make it more fast,
        // we give it bigger tolerance rate and limited small iterations.
        newGrid = sampler.sample(normCanvas,
                                 photos,
                                 0.75f,
                                 10,
                                 0);
        if (newGrid.size()) {
            std::ostringstream nameStream;

            // We steal the generated grids from other algorithm but we want
            // the realGrid name for this algorithm.
            nameStream << gGridName
                       << "pt " << ptNum << " + "
                       << "ls " << lsNum << " + "
                       << "sq " << sqNum;
            newGrid[0].name = nameStream.str();
            break;
        }
        --maxTrials;
    }

    // FIXME: Not sure why the simply vector copying isn't working...
//    grids.insert(grids.end(), newGrid.begin(), newGrid.end());
    for (int i = 0; i < newGrid.size(); ++i) {
        outGrids.push_back(newGrid[i]);
    }
}
