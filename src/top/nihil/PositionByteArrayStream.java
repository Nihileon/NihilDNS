package top.nihil;

import java.io.ByteArrayInputStream;

public class PositionByteArrayStream extends ByteArrayInputStream {
    public PositionByteArrayStream(byte[] buf) {
        super(buf);
    }

    public int getPosition(){
        return this.pos;
    }
}
