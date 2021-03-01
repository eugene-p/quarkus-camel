package org.yp.camel;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class ChecksumAggregationStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }

        Boolean FileExists = false;
        if (newExchange.getMessage().getHeader("FileExists", Boolean.class) != null) {
            FileExists = newExchange.getMessage().getHeader("FileExists", Boolean.class);
        }

        if(oldExchange.getMessage().getHeader("FileExists", Boolean.class) != null) {
            FileExists = oldExchange.getMessage().getHeader("FileExists", Boolean.class);
        }

        String oldBody = oldExchange.getMessage().getBody(String.class);
        String newBody = newExchange.getMessage().getBody(String.class);
        String theBody = "";

        if (oldBody.length() > newBody.length()) {
            theBody = oldBody;
        } else {
            theBody = newBody;
        }

        oldExchange.getMessage().setHeader("FileExists", FileExists);
        oldExchange.getMessage().setBody(theBody);
        return oldExchange;
    }
}