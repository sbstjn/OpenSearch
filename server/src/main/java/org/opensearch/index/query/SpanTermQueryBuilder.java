/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.opensearch.common.ParseField;
import org.opensearch.common.ParsingException;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.lucene.BytesRefs;
import org.opensearch.common.xcontent.XContentParser;
import org.opensearch.index.mapper.MappedFieldType;

import java.io.IOException;

/**
 * A Span Query that matches documents containing a term.
 * @see SpanTermQuery
 */
public class SpanTermQueryBuilder extends BaseTermQueryBuilder<SpanTermQueryBuilder> implements SpanQueryBuilder {
    public static final String NAME = "span_term";

    private static final ParseField TERM_FIELD = new ParseField("term");

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, String) */
    public SpanTermQueryBuilder(String name, String value) {
        super(name, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, int) */
    public SpanTermQueryBuilder(String name, int value) {
        super(name, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, long) */
    public SpanTermQueryBuilder(String name, long value) {
        super(name, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, float) */
    public SpanTermQueryBuilder(String name, float value) {
        super(name, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, double) */
    public SpanTermQueryBuilder(String name, double value) {
        super(name, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, Object) */
    public SpanTermQueryBuilder(String name, Object value) {
        super(name, value);
    }

    /**
     * Read from a stream.
     */
    public SpanTermQueryBuilder(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    protected SpanQuery doToQuery(QueryShardContext context) throws IOException {
        MappedFieldType mapper = context.fieldMapper(fieldName);
        Term term;
        if (mapper == null) {
            term = new Term(fieldName, BytesRefs.toBytesRef(value));
        } else {
            Query termQuery = mapper.termQuery(value, context);
            term = MappedFieldType.extractTerm(termQuery);
        }
        return new SpanTermQuery(term);
    }

    public static SpanTermQueryBuilder fromXContent(XContentParser parser) throws IOException, ParsingException {
        String fieldName = null;
        Object value = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String queryName = null;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else {
                        if (TERM_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            value = maybeConvertToBytesRef(parser.objectBytes());
                        } else if (BaseTermQueryBuilder.VALUE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            value = maybeConvertToBytesRef(parser.objectBytes());
                        } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            boost = parser.floatValue();
                        } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            queryName = parser.text();
                        } else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    "[span_term] query does not support [" + currentFieldName + "]");
                        }
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = parser.currentName();
                value = maybeConvertToBytesRef(parser.objectBytes());
            }
        }

        SpanTermQueryBuilder result = new SpanTermQueryBuilder(fieldName, value);
        result.boost(boost).queryName(queryName);
        return result;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }
}
