#ifndef CB_ALGORITHMS_LIB_IGRIDSSAMPLER_H
#define CB_ALGORITHMS_LIB_IGRIDSSAMPLER_H

#include <string>
#include <vector>

#include <include/Grid.hpp>
#include <include/Photo.hpp>

#define LAYOUT_IGNORED                  0x16050000
/**
 * The layout policy for fitting something center inside the desired canvas.
 */
#define LAYOUT_CENTER_INSIDE            0x16050001
/**
 * The layout policy for scaling something to fill the desired canvas.
 */
#define LAYOUT_SCALE_TO_FILL            0x16050002

/**
 * Generate grids with the slot number exactly equal to the photo number.
 */
#define GEN_POLICY_SLOTS_EXACTLY_EQUAL_TO_PHOTOS    0x00000000
/**
 * Generate grid with the slot number equal or larger than the photo number.
 */
#define GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS  0x00000001

/**
 * Check the bit-mask flag.
 */
#define F_CHECK(flags, flag) ((flags & flag) == flag)

class IGridsGenerator {
public:

    const int layoutPolicy;
    const int genPolicy;

    IGridsGenerator(int layoutPolicy,
                    int genPolicy)
            : layoutPolicy(layoutPolicy),
              genPolicy(genPolicy) {}

    virtual ~IGridsGenerator() {}

    /**
     * Generate a sample of grid list in terms of the given canvas size and
     * photos.
     *
     * Note:
     * The algorithm impl should NEVER alter the order of the photo list.
     * If have to, please copy the list and then sort it.
     *
     * @param canvas The canvas size.
     * @param photos The photo list.
     */
    virtual std::vector<Grid> sample(RectF& canvas,
                                     std::vector<Photo>& photos) = 0;

    /**
     * Append the source grid list to the target grid list.
     */
    virtual void append(std::vector<Grid>& target,
                        std::vector<Grid>& source) {
        // FIXME: Somehow the insert doesn't work because the iterator type.
//        grids.insert(grids.end(), source.begin(), source.end());
        for (std::vector<Grid>::iterator i = source.begin();
             i != source.end(); ++i) {
            target.push_back(*i);
        }
    }
};

#endif //CB_ALGORITHMS_LIB_IGRIDSSAMPLER_H
