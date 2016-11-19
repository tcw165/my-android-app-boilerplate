#ifndef CB_ALGORITHMS_LIB_MATH_HPP
#define CB_ALGORITHMS_LIB_MATH_HPP

#include <cmath>

#define MATH_APPROX_EQUAL_EPSILON   0.01f
#define MATH_RAND_MAX               0x7fffffff

static unsigned int sSeed = 1;

class Math {
private:

public:
    static bool approximatelyEqual(float a,
                                   float b,
                                   float epsilon) {
        return std::abs(a - b) <= ((std::abs(a) < std::abs(b) ? std::abs(b) : std::abs(a)) * epsilon);
    }

    static bool essentiallyEqual(float a,
                                 float b,
                                 float epsilon) {
        return std::abs(a - b) <= ((std::abs(a) > std::abs(b) ? std::abs(b) : std::abs(a)) * epsilon);
    }

    static bool definitelyGreaterThan(float a,
                                      float b,
                                      float epsilon) {
        return (a - b) > ((std::abs(a) < std::abs(b) ? std::abs(b) : std::abs(a)) * epsilon);
    }

    static bool definitelyLessThan(float a,
                                   float b,
                                   float epsilon) {
        return (b - a) > ((std::abs(a) < std::abs(b) ? std::abs(b) : std::abs(a)) * epsilon);
    }

    /**
     * The pseudo-random number generator is initialized using the argument
     * passed as seed.
     * This is for some Android devices which doesn't support built-in rand()
     * function under Ver.4.4.
     * Reference: https://en.wikipedia.org/wiki/Linear_congruential_generator
     */
    static void srand(int newseed) {
        sSeed = static_cast<unsigned>(newseed) & MATH_RAND_MAX;
    }

    /**
     * Returns a pseudo-random integral number.
     * This is for some Android devices which doesn't support built-in rand()
     * function under Ver.4.4.
     * Reference: https://en.wikipedia.org/wiki/Linear_congruential_generator
     */
    static int rand(void) {
        sSeed = (sSeed * 1103515245U + 12345U) & MATH_RAND_MAX;
        return static_cast<int>(sSeed);
    }
};

#endif //CB_ALGORITHMS_LIB_MATH_HPP
