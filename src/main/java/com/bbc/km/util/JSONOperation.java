package com.bbc.km.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

/**
 * Create custom aggregation pipeline using plain json syntax
 *
 * @since 2
 * @see <a href="https://www.mongodb.com/docs/manual/aggregation/">Mongo Aggregation Official Doc</a>
 */
public class JSONOperation implements AggregationOperation {

    private final String pipeline;

    private JSONOperation(String pipeline) {
        this.pipeline = pipeline;
    }

    public static AggregationOperation of(String pipeline) {
        return new JSONOperation(pipeline);
    }

    @Override
    public Document toDocument(AggregationOperationContext context) {
        return context.getMappedObject(org.bson.Document.parse(pipeline));
    }
}
