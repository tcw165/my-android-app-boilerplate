/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.contrib.android;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper for the platform log function, allows convenient message prefixing
 * and log disabling.
 */
@SuppressWarnings("unused")
public final class Logger {
    private static final String DEFAULT_TAG = "tf";
    private static final int DEFAULT_MIN_LOG_LEVEL = Log.DEBUG;

    // Classes to be ignored when examining the stack trace
    private static final Set<String> IGNORED_CLASS_NAMES;

    static {
        IGNORED_CLASS_NAMES = new HashSet<>(3);
        IGNORED_CLASS_NAMES.add("dalvik.system.VMStack");
        IGNORED_CLASS_NAMES.add("java.lang.Thread");
        IGNORED_CLASS_NAMES.add(Logger.class.getCanonicalName());
    }

    private final String TAG;
    private final String mMsgPrefix;
    private int mMinLogLevel = DEFAULT_MIN_LOG_LEVEL;

    // For profiling the performance.
    private AtomicBoolean mIsProfilingRunning = new AtomicBoolean(false);
    private String mProfilingMsg;
    private long mProfilingStart;

    /**
     * Creates a Logger using the class name as the message prefix.
     *
     * @param clazz the simple name of this class is used as the message prefix.
     */
    @SuppressWarnings("unused")
    public Logger(final Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    /**
     * Creates a Logger using the specified message prefix.
     *
     * @param messagePrefix is prepended to the text of every message.
     */
    @SuppressWarnings("unused")
    public Logger(final String messagePrefix) {
        this(DEFAULT_TAG, messagePrefix);
    }

    /**
     * Creates a Logger with a custom TAG and a custom message prefix. If the message prefix
     * is set to <pre>null</pre>, the caller's class name is used as the prefix.
     *
     * @param tag           identifies the source of a log message.
     * @param messagePrefix prepended to every message if non-null. If null, the name of the caller is
     *                      being used
     */
    @SuppressWarnings("unused")
    public Logger(final String tag, final String messagePrefix) {
        this.TAG = tag;
        final String prefix = messagePrefix == null ? getCallerSimpleName() : messagePrefix;
        this.mMsgPrefix = (prefix.length() > 0) ? prefix + ": " : prefix;
    }

    /**
     * Creates a Logger using the caller's class name as the message prefix.
     */
    @SuppressWarnings("unused")
    public Logger() {
        this(DEFAULT_TAG, null);
    }

    /**
     * Creates a Logger using the caller's class name as the message prefix.
     */
    @SuppressWarnings("unused")
    public Logger(final int minLogLevel) {
        this(DEFAULT_TAG, null);
        this.mMinLogLevel = minLogLevel;
    }

    @SuppressWarnings("unused")
    public void setmMinLogLevel(final int mMinLogLevel) {
        this.mMinLogLevel = mMinLogLevel;
    }

    @SuppressWarnings("unused")
    public boolean isLoggable(final int logLevel) {
        return logLevel >= mMinLogLevel || Log.isLoggable(TAG, logLevel);
    }

    @SuppressWarnings("unused")
    public void v(final String format, final Object... args) {
        if (isLoggable(Log.VERBOSE)) {
            Log.v(TAG, toMessage(format, args));
        }
    }

    @SuppressWarnings("unused")
    public void v(final Throwable t, final String format, final Object... args) {
        if (isLoggable(Log.VERBOSE)) {
            Log.v(TAG, toMessage(format, args), t);
        }
    }

    @SuppressWarnings("unused")
    public void d(final String format, final Object... args) {
        if (isLoggable(Log.DEBUG)) {
            Log.d(TAG, toMessage(format, args));
        }
    }

    @SuppressWarnings("unused")
    public void d(final Throwable t, final String format, final Object... args) {
        if (isLoggable(Log.DEBUG)) {
            Log.d(TAG, toMessage(format, args), t);
        }
    }

    @SuppressWarnings("unused")
    public void i(final String format, final Object... args) {
        if (isLoggable(Log.INFO)) {
            Log.i(TAG, toMessage(format, args));
        }
    }

    @SuppressWarnings("unused")
    public void i(final Throwable t, final String format, final Object... args) {
        if (isLoggable(Log.INFO)) {
            Log.i(TAG, toMessage(format, args), t);
        }
    }

    @SuppressWarnings("unused")
    public void w(final String format, final Object... args) {
        if (isLoggable(Log.WARN)) {
            Log.w(TAG, toMessage(format, args));
        }
    }

    @SuppressWarnings("unused")
    public void w(final Throwable t, final String format, final Object... args) {
        if (isLoggable(Log.WARN)) {
            Log.w(TAG, toMessage(format, args), t);
        }
    }

    @SuppressWarnings("unused")
    public void e(final String format, final Object... args) {
        if (isLoggable(Log.ERROR)) {
            Log.e(TAG, toMessage(format, args));
        }
    }

    @SuppressWarnings("unused")
    public void e(final Throwable t, final String format, final Object... args) {
        if (isLoggable(Log.ERROR)) {
            Log.e(TAG, toMessage(format, args), t);
        }
    }

    @SuppressWarnings("unused")
    public synchronized void profilingStart(String msg) {
        if (mIsProfilingRunning.get()) return;

        mProfilingMsg = msg;
        mProfilingStart = System.currentTimeMillis();
        mIsProfilingRunning.set(true);
    }

    @SuppressWarnings("unused")
    public synchronized void profilingEnd() {
        final long duration = System.currentTimeMillis() - mProfilingStart;
        d("%s (takes %d ms)", mProfilingMsg, duration);
        mProfilingMsg = null;
        mProfilingStart = 0;
        mIsProfilingRunning.set(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * Return caller's simple name.
     * <p>
     * Android getStackTrace() returns an array that looks like this:
     * stackTrace[0]: dalvik.system.VMStack
     * stackTrace[1]: java.lang.Thread
     * stackTrace[2]: com.google.android.apps.unveil.env.UnveilLogger
     * stackTrace[3]: com.google.android.apps.unveil.BaseApplication
     * <p>
     * This function returns the simple version of the first non-filtered name.
     *
     * @return caller's simple name
     */
    @SuppressWarnings("unused")
    private static String getCallerSimpleName() {
        // Get the current callstack so we can pull the class of the caller off of it.
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (final StackTraceElement elem : stackTrace) {
            final String className = elem.getClassName();
            if (!IGNORED_CLASS_NAMES.contains(className)) {
                // We're only interested in the simple name of the class, not the complete package.
                final String[] classParts = className.split("\\.");
                return classParts[classParts.length - 1];
            }
        }

        return Logger.class.getSimpleName();
    }

    @SuppressWarnings("unused")
    private String toMessage(final String format, final Object... args) {
        return mMsgPrefix + (args.length > 0 ? String.format(format, args) : format);
    }
}
