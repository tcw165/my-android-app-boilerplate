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

package com.my.demo.doodle.data;

import com.my.demo.doodle.protocol.ISketchBrush;
import com.my.demo.doodle.protocol.ISketchStroke;

import java.util.ArrayList;
import java.util.List;

public class ColorPenBrushFactory {

    // Config to build the shared stroke.
    private float mMinPathSegmentLength = 10f;
    private long mMinPathSegmentDuration = 10L;

    private final List<Integer> mColors = new ArrayList<>();

    public ColorPenBrushFactory setMinPathSegmentLength(final float length) {
        mMinPathSegmentLength = length;

        return this;
    }

    public ColorPenBrushFactory setMinPathSegmentDuration(final long duration) {
        mMinPathSegmentDuration = duration;

        return this;
    }

    public ColorPenBrushFactory addColor(final int color) {
        mColors.add(color);

        return this;
    }

    public List<ISketchBrush> build() {
        final List<ISketchBrush> brushes = new ArrayList<>();

        // Ensure the shared stroke.
        final PenSketchStroke sharedStroke = new PenSketchStroke(
            mMinPathSegmentLength, mMinPathSegmentDuration);

        // Construct the brushes.
        for (Integer color : mColors) {
            brushes.add(new PenBrushWithSharedStroke(sharedStroke, color));
        }

        return brushes;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class PenBrushWithSharedStroke
        implements ISketchBrush {

        private final ISketchStroke mStroke;
        private final int mStrokeColor;

        private PenBrushWithSharedStroke(final ISketchStroke stroke,
                                         final int color) {
            mStroke = stroke;
            mStrokeColor = color;
        }

        @Override
        public void setStroke(ISketchStroke stroke) {
            // DUMMY IMPL, it's not allowed to change stroke after first time
            // instantiation.
        }

        @Override
        public ISketchStroke getStroke() {
            mStroke.setColor(mStrokeColor);
            return mStroke;
        }
    }
}
