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

package com.ning.arecibo.util.timeline;

import org.joda.time.DateTime;

import com.ning.arecibo.util.timeline.samples.SampleConsumer;
import com.ning.arecibo.util.timeline.samples.SampleOpcode;

public class CSVSampleConsumer implements SampleConsumer
{
    private final StringBuilder builder = new StringBuilder();
    // Use our private counter because of the decimating filter
    private int builderSampleNumber = 0;

    public CSVSampleConsumer()
    {
    }

    @Override
    public void consumeSample(final int sampleNumber, final SampleOpcode opcode, final Object value, final DateTime time)
    {
        if (time != null) {
            final String valueString = value == null ? "0" : value.toString();
            if (builderSampleNumber > 0) {
                builder.append(",");
            }

            builder
                    .append(DateTimeUtils.unixSeconds(time))
                    .append(",")
                    .append(valueString);
            builderSampleNumber++;
        }
    }

    @Override
    public synchronized String toString()
    {
        final String value = builder.toString();
        // Allow for re-use
        builder.setLength(0);
        builderSampleNumber = 0;
        return value;
    }
}
