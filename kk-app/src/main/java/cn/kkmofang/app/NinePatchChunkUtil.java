package cn.kkmofang.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by jiasen on 2018/4/10.
 */

public class NinePatchChunkUtil {

    public static byte[] getChunk(int capLeft, int capTop, int step_length) {
        int[] xRegions = new int[]{capLeft, capLeft + step_length};
        int[] yRegions = new int[]{capTop, capTop + step_length};
        int NO_COLOR = 0x00000001;
        int colorSize = 9;
        int bufferSize = xRegions.length * 4 + yRegions.length * 4 + colorSize * 4 + 32;

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.nativeOrder());
// 第一个byte，要不等于0
        byteBuffer.put((byte) 1);

//mDivX step_length
        byteBuffer.put((byte) 2);
//mDivY step_length
        byteBuffer.put((byte) 2);
//mColors step_length
        byteBuffer.put((byte) colorSize);

//skip
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);

//padding 先设为0
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);

//skip
        byteBuffer.putInt(0);

// mDivX
        byteBuffer.putInt(xRegions[0]);
        byteBuffer.putInt(xRegions[1]);

// mDivY
        byteBuffer.putInt(yRegions[0]);
        byteBuffer.putInt(yRegions[1]);

// mColors
        for (int i = 0; i < colorSize; i++) {
            byteBuffer.putInt(NO_COLOR);
        }

        return byteBuffer.array();
    }
}
