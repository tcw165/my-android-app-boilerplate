#ifndef CB_ALGORITHMS_LIB_DEVSIGNER_EQUAL_GRID_SAMPLER_H
#define CB_ALGORITHMS_LIB_DEVSIGNER_EQUAL_GRID_SAMPLER_H

#ifdef _WINRT_DLL
#include <ExtDefs.h>
#endif // _WINRT_DLL

#include <Grid.hpp>
#include <Photo.hpp>
#include <IGridsGenerator.hpp>
#include <algorithm/one_big/common.hpp>

class EqualGridSampler : public IGridsGenerator {
public:
    EqualGridSampler(int layoutPolicy,
                     int genPolicy);

    virtual ~EqualGridSampler();

    virtual std::vector<Grid> sample(RectF& canvas,
                                     std::vector<Photo>& photos);
};

#endif //CB_ALGORITHMS_LIB_DEVSIGNER_EQUAL_GRID_SAMPLER_H
