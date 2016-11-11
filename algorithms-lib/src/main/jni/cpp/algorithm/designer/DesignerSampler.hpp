#ifndef CB_ALGORITHMS_LIB_DESIGNERSAMPLER_H
#define CB_ALGORITHMS_LIB_DESIGNERSAMPLER_H

#include <vector>
#include <Photo.hpp>
#include <Grid.hpp>
#include <IGridsGenerator.hpp>
#include <algorithm/pic_wall_6694305/PicWallSampler.hpp>

class DesignerSampler : public IGridsGenerator {
public:
    DesignerSampler(int layoutPolicy,
                    int genPolicy);

    ~DesignerSampler();

    std::vector<Grid> sample(RectF& canvas,
                             std::vector<Photo>& photos);

protected:
    std::vector<std::vector<Grid> > mBucket;

    /**
     * Specify the amount of protrait/landscape/squares slots and the number of
     * grids you want.
     *
     * @param ptNum Amount of the desired portrait slots.
     * @param lsNum Amount of the desired landscape slots.
     * @param sqNum Amount of the desired square slots.
     * @param outGrids The grid list where the changes will be written to.
     */
    void genDefault(size_t ptNum,
                    size_t lsNum,
                    size_t sqNum,
                    std::vector<Grid>& outGrids);
};

#endif //CB_ALGORITHMS_LIB_DESIGNERSAMPLER_H
