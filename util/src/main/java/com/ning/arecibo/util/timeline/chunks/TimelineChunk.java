/*
 * Copyright 2010-2012 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.arecibo.util.timeline.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonView;
import org.joda.time.DateTime;

import com.ning.arecibo.util.Logger;
import com.ning.arecibo.util.timeline.CSVSampleConsumer;
import com.ning.arecibo.util.timeline.DecimatingSampleFilter;
import com.ning.arecibo.util.timeline.samples.SampleCoder;
import com.ning.arecibo.util.timeline.samples.SampleConsumer;
import com.ning.arecibo.util.timeline.samples.SampleOpcode;
import com.ning.arecibo.util.timeline.times.TimeRangeSampleProcessor;

/**
 * Instances of this class represent timeline sequences read from the database
 * for a single host and single sample kind.  The samples are held in a byte
 * array.
 */
public class TimelineChunk
{
    private static final Logger log = Logger.getCallersLoggerViaExpensiveMagic();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SampleCoder sampleCoder;

    @JsonProperty
    @JsonView(TimelineChunksViews.Base.class)
    private final long chunkId;
    @JsonProperty
    @JsonView(TimelineChunksViews.Base.class)
    private final int hostId;
    @JsonProperty
    @JsonView(TimelineChunksViews.Base.class)
    private final int sampleKindId;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final DateTime startTime;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final DateTime endTime;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final byte[] times;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final byte[] samples;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final int sampleCount;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final int aggregationLevel;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final boolean notValid;
    @JsonProperty
    @JsonView(TimelineChunksViews.Compact.class)
    private final boolean dontAggregate;

    public TimelineChunk(final SampleCoder sampleCoder, final long chunkId, final int hostId, final int sampleKindId, final DateTime startTime, final DateTime endTime, final byte[] times, final byte[] samples, final int sampleCount)
    {
        this.sampleCoder = sampleCoder;
        this.chunkId = chunkId;
        this.hostId = hostId;
        this.sampleKindId = sampleKindId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.times = times;
        this.samples = samples;
        this.sampleCount = sampleCount;
        aggregationLevel = 0;
        notValid = false;
        dontAggregate = false;
    }

    public TimelineChunk(final SampleCoder sampleCoder, final long chunkId, final int hostId, final int sampleKindId, final DateTime startTime, final DateTime endTime,
            final byte[] times, final byte[] samples, final int sampleCount, final int aggregationLevel, final boolean notValid, final boolean dontAggregate)
    {
        this.sampleCoder = sampleCoder;
        this.chunkId = chunkId;
        this.hostId = hostId;
        this.sampleKindId = sampleKindId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.times = times;
        this.samples = samples;
        this.sampleCount = sampleCount;
        this.aggregationLevel = aggregationLevel;
        this.notValid = notValid;
        this.dontAggregate = dontAggregate;
    }

    public TimelineChunk(final SampleCoder sampleCoder, final long chunkId, final TimelineChunk other)
    {
        this.sampleCoder = sampleCoder;
        this.chunkId = chunkId;
        this.hostId = other.hostId;
        this.sampleKindId = other.sampleKindId;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.times = other.times;
        this.samples = other.samples;
        this.sampleCount = other.sampleCount;
        this.aggregationLevel = other.aggregationLevel;
        this.notValid = other.notValid;
        this.dontAggregate = other.dontAggregate;
    }

    @JsonView(TimelineChunksViews.Loose.class)
    public String getSamplesAsCSV() throws IOException
    {
        return getSamplesAsCSV(null, null);
    }

    public String getSamplesAsCSV(final DecimatingSampleFilter rangeSampleProcessor) throws IOException
    {
        sampleCoder.scan(this, rangeSampleProcessor);
        return rangeSampleProcessor.getSampleConsumer().toString();
    }

    public String getSamplesAsCSV(@Nullable final DateTime startTime, @Nullable final DateTime endTime) throws IOException
    {
        final CSVOutputProcessor processor = new CSVOutputProcessor(startTime, endTime);
        sampleCoder.scan(this, processor);
        return processor.toString();
    }

    public String toString()
    {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final JsonGenerator generator = objectMapper.getJsonFactory().createJsonGenerator(out);
            generator.writeStartObject();

            generator.writeFieldName("sampleKindId");
            generator.writeNumber(sampleKindId);

            generator.writeFieldName("samples");
            generator.writeString(getSamplesAsCSV());

            generator.writeEndObject();
            generator.close();
            return out.toString();
        }
        catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    private static final class CSVOutputProcessor extends TimeRangeSampleProcessor
    {
        private final SampleConsumer delegate = new CSVSampleConsumer();
        private int sampleNumber = 0;

        public CSVOutputProcessor(@Nullable final DateTime startTime, @Nullable final DateTime endTime)
        {
            super(startTime, endTime);
        }

        @Override
        public void processOneSample(final DateTime sampleTimestamp, final SampleOpcode opcode, final Object value)
        {
            delegate.consumeSample(sampleNumber, opcode, value, sampleTimestamp);
            sampleNumber++;
        }

        @Override
        public String toString()
        {
            return delegate.toString();
        }
    }

    public long getChunkId()
    {
        return chunkId;
    }

    public int getHostId()
    {
        return hostId;
    }

    public int getSampleKindId()
    {
        return sampleKindId;
    }

    public DateTime getStartTime()
    {
        return startTime;
    }

    public DateTime getEndTime()
    {
        return endTime;
    }

    public byte[] getSamples()
    {
        return samples;
    }

    public byte[] getTimes()
    {
        return times;
    }

    public int getSampleCount()
    {
        return sampleCount;
    }

    public int getAggregationLevel()
    {
        return aggregationLevel;
    }

    public boolean getNotValid()
    {
        return notValid;
    }

    public boolean getDontAggregate()
    {
        return dontAggregate;
    }
}
