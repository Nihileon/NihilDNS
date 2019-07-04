package top.nihil;
/*
    +---------------------+
    |        Header       |
    +---------------------+
    |       Question      | the question for the name server
    +---------------------+
    |        Answer       | RRs answering the question
    +---------------------+
    |      Authority      | RRs pointing toward an authority
    +---------------------+
    |      Additional     | RRs holding additional information
    +---------------------+
 */
public class DNSMessage {
    private DNSHeader dnsHeader;
    private DNSQuestion dnsQuestion;
    private DNSResourceRecord answer;
    private DNSResourceRecord authority;


}
