/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * A classifier specialized to label images using TensorFlow.
 */
public class TensorFlowImageClassifier implements Classifier {

    private static final String TAG = "TF";

    // Logger.
    private Logger mLogger = new Logger();

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;

    // Config values.
    private String mInputName;
    private String mOutputName;
    private int mInputSize;
    private int mImageMean;
    private float mImageStd;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<>();
    private int[] mIntValues;
    private float[] mFloatValues;
    private float[] mOutputs;
    private String[] mOutputNames;

    private TensorFlowInferenceInterface mTfInterface;

    private TensorFlowImageClassifier() {
        // DO NOTHING.
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param numClasses    The number of classes output by the model.
     * @param inputSize     The input size. A square image of mInputSize x mInputSize is assumed.
     * @param imageMean     The assumed mean of the image values.
     * @param imageStd      The assumed std of the image values.
     * @param inputName     The label of the image input node.
     * @param outputName    The label of the output node.
     * @throws IOException
     */
    public static Classifier create(AssetManager assetManager,
                                    String modelFilename,
                                    String labelFilename,
                                    int numClasses,
                                    int inputSize,
                                    int imageMean,
                                    float imageStd,
                                    String inputName,
                                    String outputName)
        throws IOException {
        TensorFlowImageClassifier c = new TensorFlowImageClassifier();
        c.mInputName = inputName;
        c.mOutputName = outputName;

        // Read the label names into memory.
        // TODO(andrewharp): make this handle non-assets.
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        c.mLogger.i("Reading labels from: " + actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
        String line;
        while ((line = br.readLine()) != null) {
            c.labels.add(line);
        }
        br.close();
        c.mLogger.i("Read " + c.labels.size() + " labels, " + numClasses +
                    " output layer size specified");

        c.mInputSize = inputSize;
        c.mImageMean = imageMean;
        c.mImageStd = imageStd;

        // Pre-allocate buffers.
        c.mOutputNames = new String[]{outputName};
        c.mIntValues = new int[inputSize * inputSize];
        c.mFloatValues = new float[inputSize * inputSize * 3];
        c.mOutputs = new float[numClasses];

        c.mTfInterface = new TensorFlowInferenceInterface();

        final int status = c.mTfInterface.initializeTensorFlow(assetManager, modelFilename);
        if (status != 0) {
            Log.e(TAG, "TF init status: " + status);
            throw new RuntimeException("TF init status (" + status + ") != 0");
        }
        return c;
    }

    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Pre-process the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(mIntValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < mIntValues.length; ++i) {
            final int val = mIntValues[i];
            mFloatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - mImageMean) / mImageStd;
            mFloatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - mImageMean) / mImageStd;
            mFloatValues[i * 3 + 2] = ((val & 0xFF) - mImageMean) / mImageStd;
        }

        // Copy the input data into TensorFlow.
        mTfInterface.fillNodeFloat(mInputName,
                                   new int[]{1, mInputSize, mInputSize, 3},
                                   mFloatValues);

        // Run the inference call.
        mTfInterface.runInference(mOutputNames);

        // Copy the output Tensor back into the output array.
        mTfInterface.readNodeFloat(mOutputName, mOutputs);

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
            new PriorityQueue<>(
                3,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        // Intentionally reversed to put high confidence at the head of the queue.
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    }
                });
        for (int i = 0; i < mOutputs.length; ++i) {
            if (mOutputs[i] > THRESHOLD) {
                pq.add(new Recognition("" + i,
                                       labels.size() > i ? labels.get(i) : "unknown", mOutputs[i], null));
            }
        }
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    @Override
    public void enableStatLogging(boolean debug) {
        mTfInterface.enableStatLogging(debug);
    }

    @Override
    public String getStatString() {
        return mTfInterface.getStatString();
    }

    @Override
    public void close() {
        mTfInterface.close();
    }
}
