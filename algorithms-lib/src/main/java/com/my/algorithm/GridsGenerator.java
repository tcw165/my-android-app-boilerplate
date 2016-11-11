package com.my.algorithm;

import android.util.Log;

import com.my.algorithm.proto.ProtoGrid.MsgGrid;
import com.my.algorithm.proto.ProtoGridList.MsgGridList;
import com.my.algorithm.proto.ProtoPhoto.MsgPhoto;
import com.my.algorithm.proto.ProtoPhotoList.MsgPhotoList;
import com.my.algorithm.proto.ProtoRectF.MsgRectF;

import java.util.Collections;
import java.util.List;

public class GridsGenerator {

    static {
        try {
            Log.d("jni", "Load algorithms-lib...");
            System.loadLibrary("algorithms-lib");
            Log.d("jni", "done!");
        } catch (Throwable ex) {
            // DO NOTHING.
            Log.d("jni", String.format("Can't load library, error: %s", ex.getMessage()));
        }
    }

    public static final int ALGO_DESIGNER_SAMPLER               = 0x16080001;
    public static final int ALGO_PIC_WALL_SAMPLER               = 0x16080002;
    public static final int ALGO_PACK_GRID_DP_SAMPLER           = 0x16080004;
    public static final int ALGO_EQUAL_GRID_SAMPLER             = 0x16080008;
    public static final int ALGO_BIG_CENTER_GRID_SAMPLER        = 0x16080010;
    public static final int ALGO_BIG_TOP_GRID_SAMPLER           = 0x16080020;
    public static final int ALGO_BIG_LEFT_TOP_GRID_SAMPLER      = 0x16080040;

    public static final int GEN_POLICY_SLOTS_EXACTLY_EQUAL_TO_PHOTOS    = 0x00000000;
    public static final int GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS  = 0x00000001;

    protected static final String TAG = GridsGenerator.class.getSimpleName();

    public static List<MsgGrid> generate(MsgRectF canvas,
                                         List<MsgPhoto> photos) {
        return generate(canvas,
                        photos,
                        ALGO_DESIGNER_SAMPLER |
                        ALGO_PIC_WALL_SAMPLER |
                        ALGO_PACK_GRID_DP_SAMPLER |
                        ALGO_EQUAL_GRID_SAMPLER |
                        ALGO_BIG_CENTER_GRID_SAMPLER |
                        ALGO_BIG_TOP_GRID_SAMPLER |
                        ALGO_BIG_LEFT_TOP_GRID_SAMPLER,
                        GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
    }

    public static List<MsgGrid> generate(MsgRectF canvas,
                                         List<MsgPhoto> photos,
                                         int useWhatAlgorithm,
                                         int genPolicy) {
        try {
            // To a proto instance.
            MsgPhotoList msgPhotoList = MsgPhotoList.newBuilder()
                                                    .addAllItems(photos)
                                                    .build();

            // Call the native generate function.
            byte[] byteGrids = generateNative(canvas.toByteArray(),
                                              msgPhotoList.toByteArray(),
                                              useWhatAlgorithm,
                                              genPolicy);

            // Deserialize from the byte[].
            MsgGridList msgGrids = MsgGridList.parseFrom(byteGrids);
            // To Java list.
            List<MsgGrid> outGrids = msgGrids.getItemsList();

            return outGrids;
        } catch (Throwable ex) {
            Log.d(TAG, ex.toString());
            return Collections.emptyList();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * Native grids-generator.
     *
     * @param canvas           The byte[] serialized from {@code MsgRectF}.
     * @param photos           The byte[] serialized from {@code MsgPhotoList}.
     * @param useWhatAlgorithm The algorithm permuatation.
     *                         e.g.
     *                         ALGO_DESIGNER_SAMPLER,
     *                         ALGO_PIC_WALL_SAMPLER,
     *                         ALGO_PACK_GRID_DP_SAMPLER
     * @return The byte[] serialized from {@code MsgGridList}.
     */
    public static native byte[] generateNative(byte[] canvas,
                                               byte[] photos,
                                               int useWhatAlgorithm,
                                               int genPolicy);
}
