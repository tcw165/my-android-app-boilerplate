// Copyright (c) 2016 boyw165
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

package com.my.boilerplate.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    private final static String TAG = BitmapUtil.class.getSimpleName();

    /**
     * The maximum side length of the image to detect, to keep the size of image
     * less than 4MB. Resize the image if its side length is larger than the
     * maximum.
     */
    public static final int IMAGE_FULL_SIZE_LENGTH = Integer.MAX_VALUE;
    public static final int IMAGE_MAX_SIDE_LENGTH = 520;

    /**
     * Decode image from a given URI, and resize according to the IMAGE_MAX_SIDE_LENGTH.
     * If expectedMaxImageSideLength is
     * (1) less than or equal to 0,
     * (2) more than the actual max size length of the bitmap
     * then return the original bitmap
     * Else, return the scaled bitmap.
     *
     * @param resolver              This class provides applications access to
     *                              the content model.
     * @param imgUri                The URI to be loaded.
     * @param expectedMaxSideLength The expected maximum side length. Always
     *                              return 1 if it's equal or less than 0.
     */
    public static Bitmap getSizeLimitedBitmapFromUri(ContentResolver resolver,
                                                     Uri imgUri,
                                                     int expectedMaxSideLength)
        throws IOException {
        // Load the image into InputStream.
        InputStream imageInputStream = resolver.openInputStream(imgUri);

        // For saving memory, only decode the image meta and get the side length.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Rect outPadding = new Rect();
        BitmapFactory.decodeStream(imageInputStream, outPadding, options);

        // Calculate shrink rate when loading the image into memory.
        int maxSideLength = options.outWidth > options.outHeight ?
            options.outWidth :
            options.outHeight;
        options.inSampleSize = calculateSampleSize(maxSideLength, expectedMaxSideLength);
        options.inJustDecodeBounds = false;
        if (imageInputStream != null) {
            imageInputStream.close();
        }

        // Load the bitmap and resize it to the expected size length.
        imageInputStream = resolver.openInputStream(imgUri);
        Bitmap bitmap = BitmapFactory.decodeStream(imageInputStream, outPadding, options);
        maxSideLength = bitmap.getWidth() > bitmap.getHeight()
            ? bitmap.getWidth() : bitmap.getHeight();
        double ratio = IMAGE_MAX_SIDE_LENGTH / (double) maxSideLength;
        if (ratio < 1) {
            bitmap = Bitmap.createScaledBitmap(
                bitmap,
                (int) (bitmap.getWidth() * ratio),
                (int) (bitmap.getHeight() * ratio),
                false);
        }

        return rotateBitmap(bitmap, getImageRotationAngleFromUri(resolver, imgUri));
    }

    public static Bitmap getFullSizeBitmapFromUri(ContentResolver resolver,
                                                  Uri imgUri)
    throws IOException {
        return getSizeLimitedBitmapFromUri(resolver, imgUri, IMAGE_FULL_SIZE_LENGTH);
    }

    public static Bitmap getStraightBitmapFromCamera(byte[] data,
                                                     Camera camera) {

        // Convert raw buffer to Bitmap
        // TODO: Correct direction of given camera buffer in terms of the device.
        Camera.Parameters param = camera.getParameters();
        int width = param.getPreviewSize().width;
        int height = param.getPreviewSize().height;
        YuvImage yuvImage = new YuvImage(data, param.getPreviewFormat(),
                                         width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                                   bitmap.getWidth(),
                                                   bitmap.getHeight(),
                                                   matrix,
                                                   true);

        // Close stream.
        try {
            out.close();
        } catch (IOException ex) {
            // DO NOTHING.
        }

        return rotatedBitmap;
    }

    public static Rect getOriginalBitmapSize(ContentResolver resolver,
                                             Uri imgUri)
        throws IOException {
        // Load the image into InputStream.
        InputStream imgIs = resolver.openInputStream(imgUri);

        // For saving memory, only decode the image meta and get the side length.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(imgIs, null, options);

        // Close input-stream.
        if (imgIs != null) {
            imgIs.close();
        }

        int angle = getImageRotationAngleFromUri(resolver, imgUri);
        if (angle == 90 || angle == 270) {
            return new Rect(0, 0, options.outHeight, options.outWidth);
        } else {
            return new Rect(0, 0, options.outWidth, options.outHeight);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private ////////////////////////////////////////////////////////////////

    /**
     * Return the number of times for the image to shrink when loading it into
     * memory. The SampleSize can only be a final value based on powers of 2.
     *
     * @param maxSideLength         The given maximum side length.
     * @param expectedMaxSideLength The expected maximum side length. Always
     *                              return 1 if it's equal or less than 0.
     */
    private static int calculateSampleSize(int maxSideLength,
                                           int expectedMaxSideLength) {
        int inSampleSize = 1;

        if (expectedMaxSideLength > 0) {
            while (maxSideLength > expectedMaxSideLength) {
                maxSideLength /= 2;
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Get the rotation angle of the image taken.
     */
    private static int getImageRotationAngleFromUri(ContentResolver resolver,
                                                    Uri imgUri)
        throws IOException {
        int angle = 0;
        Cursor cursor = resolver.query(
            imgUri,
            new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
            null, null, null);

        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                angle = cursor.getInt(0);
            }
            cursor.close();
        } else {
            ExifInterface exif = new ExifInterface(imgUri.getPath());
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                default:
                    break;
            }
        }
        return angle;
    }

    /**
     * Rotate the original bitmap according to the given orientation angle
     */
    private static Bitmap rotateBitmap(Bitmap bitmap,
                                       int angle) {
        // If the rotate angle is 0, then return the original image, else return the rotated image
        if (angle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }
}
