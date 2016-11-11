#ifndef CB_ALGORITHMS_LIB_UTIL_H
#define CB_ALGORITHMS_LIB_UTIL_H

#include <cctype>
#include <vector>
#include <Grid.hpp>
#include <Photo.hpp>

class Util {
public:
    /**
     * Append the source grid list to the target grid list.
     */
    static void append(std::vector<Grid>& target,
                       std::vector<Grid>& source) {
        // FIXME: Somehow the insert doesn't work because the iterator type.
//        grids.insert(grids.end(), source.begin(), source.end());
        for (std::vector<Grid>::iterator i = source.begin();
             i != source.end(); ++i) {
            target.push_back(*i);
        }
    }

    /**
     * Check if the every photo in the photo list has non-zero size.
     */
    static bool checkPhotos(std::vector<Photo>& photos) {
        for (size_t i = 0; i < photos.size(); ++i) {
            const float EPSILON = 0.0033f;
            Photo& photo = photos[i];
            if (Math::approximatelyEqual(photo.width, 0.f, EPSILON) ||
                Math::approximatelyEqual(photo.height, 0.f, EPSILON)) {
                return false;
            }
        }

        return true;
    }
};

#endif //CB_ALGORITHMS_LIB_UTIL_H
