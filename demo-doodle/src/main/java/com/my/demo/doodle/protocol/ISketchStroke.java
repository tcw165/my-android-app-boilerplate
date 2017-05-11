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

package com.my.demo.doodle.protocol;

import android.graphics.Canvas;

import java.util.List;

/**
 * A sketch scrap could contains multiple strokes. Every stroke has an array of
 * tuple. PathTuple is the data describing the node in a path segment.
 * <br/>
 * A tuple could contains multiple x-y pairs. The design is for drawing either
 * straight line or Bezier curve.
 * <br/>
 * If it's a single element tuple, the line is straight.
 * <br/>
 * If it's a two elements tuple, the line is a Bezier curve.
 * <br/>
 * If it's a three elements tuple, the line is a Bezier curve with smoother
 * visualization.
 *
 * <pre>
 * A sketch stroke of a sketch scrap.
 * (+) is the tuple.
 * (-) is the straight/bezier line connects two tuple.
 * .-------------------.
 * |                   |
 * | +-+         +--+  |
 * |    \        |     |
 * |    +-+    +-+     |
 * |      |   /        |
 * |      +--+         |
 * |                   |
 * '-------------------'
 * </pre>
 */
public interface ISketchStroke {

    ISketchStroke setWidth(final float width);
    float getWidth();

    ISketchStroke setColor(final int color);
    int getColor();

    boolean savePathTuple(final float x,
                          final float y);

    PathTuple getPathTupleAt(final int position);
    PathTuple getFirstPathTuple();
    PathTuple getLastPathTuple();

    int size();
    List<PathTuple> getAllPathTuples();

    void draw(final Canvas canvas);

    void draw(final Canvas canvas, int start, int end);

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * A path tuple represents a path node. It may contains more than one x-y
     * pair in order to draw Bezier curve. A x-y pair is called Anchor.
     */
    interface PathTuple {

        Anchor getAnchorAt(final int position);

        List<Anchor> getAllAnchors();
    }

    /**
     * A x-y pair with additional information, like color and width.
     */
    interface Anchor {

        Anchor setX(final float x);
        float getX();

        Anchor setY(final float y);
        float getY();
    }
}
