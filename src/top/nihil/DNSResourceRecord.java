package top.nihil;

/*
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                                               |
    /                                               /
    /                      NAME                     /
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TYPE                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     CLASS                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TTL                      |
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                   RDLENGTH                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
    /                     RDATA                     /
    /                                               /
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

where:

NAME            a domain name to which this resource record pertains.

TYPE            two octets containing one of the RR type codes.  This
                field specifies the meaning of the data in the RDATA
                field.

CLASS           two octets which specify the class of the data in the
                RDATA field.

TTL             a 32 bit unsigned integer that specifies the time
                interval (in seconds) that the resource record may be
                cached before it should be discarded.  Zero values are
                interpreted to mean that the RR can only be used for the
                transaction in progress, and should not be cached.

RDLENGTH        an unsigned 16 bit integer that specifies the length in
                octets of the RDATA field.

RDATA           a variable length string of octets that describes the
                resource.  The format of this information varies
                according to the TYPE and CLASS of the resource record.
                For example, the if the TYPE is A and the CLASS is IN,
                the RDATA field is a 4 octet ARPA Internet address.
 */

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DNSResourceRecord {
    private int NAME;
    private int TYPE;
    private int CLASS;
    private long TTL;
    private int RDLENGTH;
    private String RDATA;

    public static final int QTYPE_A = (1);
    public static final int QTYPE_NS = (2);
    public static final int QTYPE_CNAME = (5);
    public static final int QTYPE_SOA = (6);
    public static final int QTYPE_MX = (15);
    public static final int QTYPE_AAAA = (28);

    public byte[] toByteArray() {
        //resource record without RDATA is 12 bytes;
        byte[] bytes = new byte[12 + RDLENGTH];

        int offset = 0;
        System.arraycopy(Converter.shortToByteArray(NAME), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(TYPE), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(CLASS), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.intToByteArray(TTL), 0, bytes, offset, 4);
        offset += 4;
        System.arraycopy(Converter.shortToByteArray(RDLENGTH), 0, bytes, offset, 2);
        offset += 2;

        if (TYPE == QTYPE_A) {
            System.arraycopy(Converter.ipv4ToByteArray(RDATA), 0, bytes, offset, RDLENGTH);
        } else if (TYPE == QTYPE_CNAME) {
            System.arraycopy(Converter.domainNameToByteArray(RDATA), 0, bytes, offset, RDLENGTH);
        }
        return bytes;
    }
}
