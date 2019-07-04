package top.nihil;

import lombok.AllArgsConstructor;
import lombok.Data;
import sun.nio.ch.sctp.MessageInfoImpl;

import java.io.IOException;

/*

                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                                               |
    /                     QNAME                     /
    /                                               /
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     QTYPE                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     QCLASS                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

where:

QNAME           a domain name represented as a sequence of labels, where
                each label consists of a length octet followed by that
                number of octets.  The domain name terminates with the
                zero length octet for the null label of the root.  Note
                that this field may be an odd number of octets; no
                padding is used.

QTYPE           a two octet code which specifies the type of the query.
                The values for this field include all codes valid for a
                TYPE field, together with some more general codes which
                can match more than one type of RR.
                TYPE            value and meaning

    A               1 a host address

    NS              2 an authoritative name server

    MD              3 a mail destination (Obsolete - use MX)

    MF              4 a mail forwarder (Obsolete - use MX)

    CNAME           5 the canonical name for an alias

    SOA             6 marks the start of a zone of authority

    MB              7 a mailbox domain name (EXPERIMENTAL)

    MG              8 a mail group member (EXPERIMENTAL)

    MR              9 a mail rename domain name (EXPERIMENTAL)

    NULL            10 a null RR (EXPERIMENTAL)

    WKS             11 a well known service description

    PTR             12 a domain name pointer

    HINFO           13 host information

    MINFO           14 mailbox or mail list information

    MX              15 mail exchange

    TXT             16 text strings

QCLASS          a two octet code that specifies the class of the query.
                For example, the QCLASS field is IN for the Internet.
 */
@AllArgsConstructor
@Data
public class DNSQuestion {
    private String QNAME;
    private int QTYPE;
    private int QCLASS;

    public static final int QCLASS_IN = (1);
    public static final int QCLASS_CS = (2);
    public static final int QCLASS_CH = (3);
    public static final int QCLASS_HS = (4);

    public static final int QTYPE_A = (1);
    public static final int QTYPE_NS = (2);
    public static final int QTYPE_CNAME = (5);
    public static final int QTYPE_MX = (15);
    public static final int QTYPE_AAAA = (28);

    public DNSQuestion(MessageDataInputStream in) throws IOException {
        QNAME = in.readDomainName();
        QTYPE = in.readUnsignedShort();
        QCLASS = in.readUnsignedShort();
    }

    public DNSQuestion() {

    }

    public String getQTYPEName() {
        switch (QTYPE) {
            case QTYPE_A:
                return "A";
            case QTYPE_AAAA:
                return "AAAA";
            case QTYPE_NS:
                return "NS";
            case QTYPE_CNAME:
                return "CNAME";
            default:
                return String.format("%d", QTYPE);
        }
    }

    public byte[] toByteArray() {
        byte[] domainNameByte  = Converter.domainNameToByteArray(QNAME);
        byte[] bytes = new byte[domainNameByte.length+4];
        int offset =0;
        System.arraycopy(domainNameByte,0,bytes,offset,domainNameByte.length);
        offset+=domainNameByte.length;
        System.arraycopy(Converter.shortToByteArray((short)QTYPE),0,bytes,offset,2);
        offset+=2;
        System.arraycopy(Converter.shortToByteArray((short)QCLASS),0,bytes,offset,2);
        return bytes;
    }

}