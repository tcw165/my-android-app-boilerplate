// Copyright (c) 2016-present boyw165
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

package com.my.core.util;

import android.graphics.Matrix;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * For testing {@link MatrixUtil}.
 */
@RunWith(AndroidJUnit4.class)
public class MatrixUtilTest {

    @Test
    public void translateXAndY_shouldBeCorrect() throws Exception {
        final float TX = 400f;
        final float TY = 300f;

        final Matrix tester = new Matrix();
        tester.postTranslate(TX, TY);

        // Method 1: The translation should be interpret correctly.
        final MatrixUtil translatorTestee = new MatrixUtil(tester);
        Assert.assertEquals(TX, translatorTestee.getTranslationX(), 0f);
        Assert.assertEquals(TY, translatorTestee.getTranslationY(), 0f);

        // Method 2: The translation should be interpret correctly.
        Assert.assertEquals(TX, MatrixUtil.getTranslationX(tester), 0f);
        Assert.assertEquals(TY, MatrixUtil.getTranslationY(tester), 0f);
    }

    @Test
    public void scaleXAndY_shouldBeCorrect() throws Exception {
        final float SX = .4f;
        final float SY = .3f;

        final Matrix tester = new Matrix();
        tester.postScale(SX, SY);

        // Method 1: The translation should be interpret correctly.
        final MatrixUtil translatorTestee = new MatrixUtil(tester);
        Assert.assertEquals(SX, translatorTestee.getScaleX(), 0f);
        Assert.assertEquals(SY, translatorTestee.getScaleY(), 0f);

        // Method 2: The translation should be interpret correctly.
        Assert.assertEquals(SX, MatrixUtil.getScaleX(tester), 0f);
        Assert.assertEquals(SY, MatrixUtil.getScaleY(tester), 0f);
    }

    @Test
    public void rotate_shouldBeCorrect() throws Exception {
        final float DEGREES = 45f;

        final Matrix tester = new Matrix();
        tester.postRotate(DEGREES);

        // The translation should be interpret correctly.
        final MatrixUtil translatorTestee = new MatrixUtil(tester);
        Assert.assertEquals(DEGREES, translatorTestee.getRotationInDegrees(), 0f);

        // Method 2: The translation should be interpret correctly.
        Assert.assertEquals(DEGREES, MatrixUtil.getRotationInDegrees(tester), 0f);
    }
}
