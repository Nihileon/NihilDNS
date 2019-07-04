package top.nihil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MessageDataInputStream extends DataInputStream {

    private Map<Integer, String> compressedDomainName = new HashMap<>();


    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param buffer the specified input stream
     */

    public MessageDataInputStream(byte[] buffer) {
        super(new PositionByteArrayStream(buffer));

    }

    public MessageDataInputStream(FileInputStream in) {
        super(in);

    }

    private String parseDomainName(String domainName) throws IOException {
        int partLength = this.readUnsignedByte();
        if (partLength >= 0b11000000) {
            int pos = ((partLength & 0b00111111) << 8) | this.readUnsignedByte();
            return compressedDomainName.get(pos);
        } else if (partLength == 0) {
            return domainName;
        } else {
            byte[] partName = new byte[partLength];
            this.read(partName);
            if (domainName == null) {
                return this.parseDomainName("" + new String(partName));
            } else {
                return this.parseDomainName(domainName + "." + new String(partName));
            }
        }
    }


    public String readDomainName() throws IOException {
        int pos = ((PositionByteArrayStream) (super.in)).getPosition();
        String domainName = parseDomainName(null);
        compressedDomainName.put(pos,domainName);
        return domainName;
    }

    public long readUnsignedInt() throws IOException {
        return this.readInt() & 0xffffffffL;
    }

}
