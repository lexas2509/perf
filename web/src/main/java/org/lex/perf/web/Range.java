package org.lex.perf.web;

import java.util.Date;

/**
* Created with IntelliJ IDEA.
* User: lexas
* Date: 04.04.14
* Time: 21:27
* To change this template use File | Settings | File Templates.
*/
public class Range {
    private Date start;
    private Date end;

    public Range(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
