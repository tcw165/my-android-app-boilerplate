#ifndef COM_MY_JNI_PROFILER_H
#define COM_MY_JNI_PROFILER_H

#include <stack>
#include <time.h>

/**
 * An util class to profile the performance of code.
 * <br/>
 * Usage:
 * <pre>
 * // Init profiler.
 * Profile profiler;
 *
 * // Start profiling
 * profiler.start();
 *
 * // Run your code...
 *
 * // Stop and calculate the interval in milliseconds.
 * double interval = profiler.stopAndGetInterval();
 * </pre>
 */
class Profiler {
private:

    std::stack<timespec> mTimespecs;

public:

    Profiler();

    ~Profiler();

    /**
     * Start profiling. The profiler put a monotonic time-stamp to the stack.
     */
    void start();

    /**
     * Stop profiling and pop the time-stamp, then return the interval in
     * milliseconds.
     */
    double stopAndGetInterval();
};


#endif //COM_MY_JNI_PROFILER_H
