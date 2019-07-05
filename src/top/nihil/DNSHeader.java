package top.nihil;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/*

                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

 */
@Data
@Accessors(chain = true)
public class DNSHeader {
    public static final int HEADER_LENGTH = 96;
    /*
    ID              A 16 bit identifier assigned by the program that
                generates any kind of query.  This identifier is copied
                the corresponding reply and can be used by the requester
                to match up replies to outstanding queries.

     */
    private int ID;

    /*
    QR              A one bit field that specifies whether this message is a
                query (0), or a response (1).
     */
    private boolean QR;

    /*
    OPCODE          A four bit field that specifies kind of query in this
                message.  This value is set by the originator of a query
                and copied into the response.  The values are:

                0               a standard query (QUERY)

                1               an inverse query (IQUERY)

                2               a server status request (STATUS)

                3-15            reserved for future use
     */
    private int OPCODE;

    /*
    AA              Authoritative Answer - this bit is valid in responses,
                and specifies that the responding name server is an
                authority for the domain name in question section.

                Note that the contents of the answer section may have
                multiple owner names because of aliases.  The AA bit
                corresponds to the name which matches the query name, or
                the first owner name in the answer section.
     */
    private boolean AA;

    /*

    TC              TrunCation - specifies that this message was truncated
                due to length greater than that permitted on the
                transmission channel.
     */
    private boolean TC;

    /*
    RD              Recursion Desired - this bit may be set in a query and
                is copied into the response.  If RD is set, it directs
                the name server to pursue the query recursively.
                Recursive query support is optional.

     */
    private boolean RD;

    /*
    RA              Recursion Available - this be is set or cleared in a
                response, and denotes whether recursive query support is
                available in the name server.
     */
    private boolean RA;

    /*
    RCODE           Response code - this 4 bit field is set as part of
                responses.  The values have the following
                interpretation:

                0               No error condition

                1               Format error - The name server was
                                unable to interpret the query.

                2               Server failure - The name server was
                                unable to process this query due to a
                                problem with the name server.

                3               Name Error - Meaningful only for
                                responses from an authoritative name
                                server, this code signifies that the
                                domain name referenced in the query does
                                not exist.

                4               Not Implemented - The name server does
                                not support the requested kind of query.

                5               Refused - The name server refuses to
                                perform the specified operation for
                                policy reasons.  For example, a name
                                server may not wish to provide the
                                information to the particular requester,
                                or a name server may not wish to perform
                                a particular operation (e.g., zone
     */
    private int RCODE;

    /*
    QDCOUNT         an unsigned 16 bit integer specifying the number of
                entries in the question section.
     */
    private int QDCOUNT;

    /*
    ANCOUNT         an unsigned 16 bit integer specifying the number of
                resource records in the answer section.
     */
    private int ANCOUNT;

    /*
    NSCOUNT         an unsigned 16 bit integer specifying the number of name
                server resource records in the authority records
                section.

     */
    private int NSCOUNT;

    /*
    ARCOUNT         an unsigned 16 bit integer specifying the number of
                resource records in the additional records section.
     */
    private int ARCOUNT;

    int flags;

    public static final int OPCODE_QUERY = 0;
    public static final int OPCODE_IQUERY = 1;
    public static final int OPCODE_STATUS = 2;

    public static final int RCODE_NO_ERROR = 0;
    public static final int RCODE_FORMAT_ERROR = 1;
    public static final int RCODE_SERVER_FAILURE = 2;
    public static final int RCODE_NAME_ERROR = 3;
    public static final int RCODE_NOT_IMPLEMENTED = 4;
    public static final int RCODE_REFUSE = 5;

    DNSHeader() {
    }

    public DNSHeader(int ID, int flags, int QDCOUNT, int ANCOUNT, int NSCOUNT, int ARCOUNT) {
        this.ID = ID;
        this.flags = flags;
        flagsToBits(flags);
        this.QDCOUNT = QDCOUNT;
        this.ANCOUNT = ANCOUNT;
        this.NSCOUNT = NSCOUNT;
        this.ARCOUNT = ARCOUNT;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[12];
        int offset = 0;
        System.arraycopy(Converter.shortToByteArray(ID), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(flags), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(QDCOUNT), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(ANCOUNT), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(NSCOUNT), 0, bytes, offset, 2);
        offset += 2;
        System.arraycopy(Converter.shortToByteArray(ARCOUNT), 0, bytes, offset, 2);
        return bytes;
    }

    private void flagsToBits(int flags) {
        RCODE = flags & 0x0f;
        flags >>= 7;
        RA = (flags & 1) == 1;
        flags >>= 1;
        RD = (flags & 1) == 1;
        flags >>= 1;
        TC = (flags & 1) == 1;
        flags >>= 1;
        AA = (flags & 1) == 1;
        flags >>= 1;
        OPCODE = flags & 0x0f;
        flags >>= 4;
        QR = (flags & 1) == 1;
    }

    public void flagsTobits() {
        flagsToBits(flags);
    }
}
