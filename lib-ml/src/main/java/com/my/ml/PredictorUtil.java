// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.ml;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.gson.GsonBuilder;
import com.my.ml.model.Mean;

import org.dmlc.mxnet.MxnetException;
import org.dmlc.mxnet.Predictor;
import org.dmlc.mxnet.Predictor.Device;
import org.dmlc.mxnet.Predictor.InputNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Use the MxNet framework to predict the class of a given image.
 * <br/>
 * Usage:
 * <pre>
 * // Prepare the NN model (stored in the memory).
 * boolean created = PredictorUtil.prepare(this,
 *                                         R.raw.symbol,
 *                                         R.raw.params,
 *                                         new String[] {"data"},
 *                                         new int[][]{{1, 3, 224, 224}});
 * if (created) {
 *     // Predict the given bitmap.
 *     float[] clazz = PredictorUtil.predict(bitmap);
 *
 *     // TODO: Make use of the clazz.
 *
 *     // Recycle the NN model.
 *     PredictorUtil.release();
 * }
 * </pre>
 */
public class PredictorUtil {

    private static final int INDEX_R = 0;
    private static final int INDEX_G = 1;
    private static final int INDEX_B = 2;

    private static Mean sMean;
    private static Predictor sPredictor;

//    public static synchronized long prepare(Context context,
//                                            File symbol,
//                                            File params,
//                                            int devType,
//                                            int devId,
//                                            String[] inputKeys,
//                                            int[][] inputShapes)
//        throws MxnetException {
//        // TODO: Complete it.
//        return 0;
//    }

    public static synchronized boolean prepare(Context context,
                                               int symbolRes,
                                               int paramsRes,
                                               String[] inputKeys,
                                               int[][] inputShapes)
        throws MxnetException {
        if (sPredictor != null || sMean != null) release();
        if (inputKeys != null && inputShapes != null &&
            inputKeys.length != inputShapes.length) {
            throw new IllegalStateException(
                "Unmatched dimension of keys and shapes.");
        }

        // Init the params.
        if (symbolRes == 0) symbolRes = R.raw.symbol;
        if (paramsRes == 0) paramsRes = R.raw.params;
        if (inputKeys == null || inputShapes == null) {
            inputKeys = new String[]{"data"};
            inputShapes = new int[][]{{1, 3, 224, 224}};
        }

        // The symbol (graph).
        final byte[] symbol = readFile(context, symbolRes);
        // The weights.
        final byte[] params = readFile(context, paramsRes);
        // Use CPU by default.
        final Device device = new Device(Device.Type.CPU, 0);
        // Indicate the input nodes.
        final InputNode[] nodes = new InputNode[inputKeys.length];
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i] = new InputNode(inputKeys[i], inputShapes[i]);
        }

        // Prepare the predictor.
        sPredictor = new Predictor(symbol, params, device, nodes);
        if (sPredictor.handle() == 0) {
            sPredictor = null;
            return false;
        }

        // Prepare the mean table.
        String meanStr = readTextFile(context, R.raw.mean);
        if (meanStr == null) return false;
        sMean = new GsonBuilder().create()
                                 .fromJson(meanStr, Mean.class);

        return true;
    }

    public static synchronized void release()
        throws MxnetException {
        if (sPredictor != null) {
            sPredictor.free();
            sPredictor = null;
        }
        sMean = null;
    }

    public static synchronized float[] predict(Bitmap bitmap)
        throws MxnetException {
        if (sPredictor == null || sPredictor.handle() == 0) {
            throw new IllegalStateException("The predictor is not initialized.");
        }
        if (sMean == null) {
            throw new IllegalStateException("The mean table is not initialized.");
        }
        if (bitmap == null) {
            throw new IllegalStateException("The bitmap is null");
        }

        final ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        final byte[] bytes = byteBuffer.array();
        // TODO: Make sure the format is ARGB_8888.
        final float[] colors = new float[bytes.length / 4 * 3];

        // Convert ARGB_8888 to float array [R,R,R, ... ,G,G,G, ..., B,B,B].
        // The conversion depends on the NN model.
        for (int i = 0; i < bytes.length; i += 4) {
            int j = i / 4;
            colors[INDEX_R * 224 * 224 + j] = (float) (((int) (bytes[INDEX_R + i])) & 0xFF) - sMean.r;
            colors[INDEX_G * 224 * 224 + j] = (float) (((int) (bytes[INDEX_G + i])) & 0xFF) - sMean.g;
            colors[INDEX_B * 224 * 224 + j] = (float) (((int) (bytes[INDEX_B + i])) & 0xFF) - sMean.b;
        }
        sPredictor.forward("data", colors);
        // TODO: Not sure getting 0 means what.
        return sPredictor.getOutput(0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private final void PredictorUtil() {
        // DO NOTHING.
    }

    private static byte[] readFile(Context context,
                                   String path) {
        // TODO: Complete it.
        return null;
    }

    private static byte[] readFile(Context context,
                                   int resId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int size;
        InputStream ins;

        try {
            ins = context.getResources().openRawResource(resId);
            while ((size = ins.read(buffer, 0, 1024)) >= 0) {
                outputStream.write(buffer, 0, size);
            }
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private static String readTextFile(Context context,
                                       int resId) {
        final InputStream inputStream = context.getResources()
                                               .openRawResource(resId);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;

        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            return null;
        }

        return byteArrayOutputStream.toString();
    }

}
