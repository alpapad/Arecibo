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

/**
 * This SampleProcessor interpolates a stream of sample values to such that the
 * number of outputs sent to the SampleConsumer is outputCount, which is less
 * than sampleCount. It works by keeping a history of scanned samples
 * representing at least one output sample, and makes a choice of what value to
 * output from those scanned samples:
 * <p>
 * The rules for sample generation are:
 * <ul>
 * <li>No averaging - - the sample returned is _always_ one of the scanned
 * samples</li>
 * <li>The output sample is always either the largest or the smallest of the
 * scanned sample values</li>
 * <li>Whether it is the largest or smallest depends on the "trend" of the
 * samples:
 * <ul>
 * <li>If they are generally high-to-low then we output the low value.</li>
 * <li>If they are generally low-to-high then we output the high value.</li>
 * </ul>
 * </ul>
 * <p>
 * The rationale for these rules is the most interesting information is the
 * peaks and valleys of measurements, and averaging is bad because it destroys
 * peaks and valleys. A consequence of these rules is that quantities that
 * bounce around a lot will generate graphs that are a solid band between the
 * min and max values. But that's really an accurate reflection of the state. To
 * get more information, you have to look at shorter time intervals. The class
 * tries hard to make good choices amount
 * <p>
 * Of course this sort of crude averaging isn't perfect, but at least it doesn't
 * destroy peaks and valleys.
 * <p>
 * TODO: Figure out if the time passed to SampleConsumer should be the time
 * of the sample or the midpoint of the times between first and last sample.
 */
public class DecimatingSampleFilter extends TimeRangeSampleProcessor {
    private final double samplesPerOutput;
    private final double outputsPerSample;
    private final int ceilSamplesPerOutput;
    private final SampleConsumer sampleConsumer;
    private final SampleState[] filterHistory;

    private double runningSum = 0.0;
    private int sampleNumber = 0;

    /**
     * Build a DecimatingSampleFilter on which you call processSamples()
     * @param startTime The start time we're considering values, or null, meaning all time
     * @param endTime The end time we're considering values, or null, meaning all time
     * @param outputCount The number of samples to generate
     * @param sampleCount The number of samples to be scanned.  sampleCount must be >= outputCount
     * @param sampleConsumer The implementor of the SampleConsumer interface
     */
    public DecimatingSampleFilter(DateTime startTime, DateTime endTime, int outputCount, int sampleCount, SampleConsumer sampleConsumer) {
        super(startTime, endTime);
        if (outputCount <= 0 || sampleCount <= 0 || outputCount > sampleCount)  {
            throw new IllegalArgumentException(String.format("In DecimatingSampleFilter constructor, outputCount is %d but sampleCount is %d", outputCount, sampleCount));
        }
        this.sampleConsumer = sampleConsumer;
        this.samplesPerOutput = (double) sampleCount / (double) outputCount;
        this.outputsPerSample = 1.0 / this.samplesPerOutput;
        ceilSamplesPerOutput = (int) Math.ceil(samplesPerOutput);
        filterHistory = new SampleState[ceilSamplesPerOutput];
    }

    @Override
    public void processOneSample(DateTime time, SampleOpcode opcode, Object value) {
        sampleNumber++;
        final SampleState sampleState = new SampleState(opcode, value, SampleCoder.getDoubleValue(opcode, value), time);
        final int historyIndex = sampleNumber % filterHistory.length;
        filterHistory[historyIndex] = sampleState;
        runningSum += outputsPerSample;
        if (runningSum >= 1.0) {
            runningSum -= 1.0;
            if (opcode == SampleOpcode.STRING) {
                // We don't have interpolation, so just output
                // this one
                sampleConsumer.consumeSample(sampleNumber, opcode, value, time);
            }
            else {
                // Time to output a sample - compare the sum of the first samples with the
                // sum of the last samples making up the output, choosing the lowest value if
                // if the first samples are larger, and the highest value if the last samples
                // are larger
                final int samplesInAverage = ceilSamplesPerOutput > 5 ? ceilSamplesPerOutput * 2 / 3 : Math.max(1, ceilSamplesPerOutput - 1);
                final int samplesLeftOut = ceilSamplesPerOutput - samplesInAverage;
                double max = Double.MIN_VALUE;
                int maxIndex = 0;
                int minIndex = 0;
                double min = Double.MAX_VALUE;
                double firstSum = 0.0;
                double lastSum = 0.0;
                for (int i=0; i<ceilSamplesPerOutput; i++) {
                    final int index = (sampleNumber + ceilSamplesPerOutput - i) % ceilSamplesPerOutput;
                    final SampleState sample = filterHistory[index];
                    if (sample != null) {
                        final double doubleValue = sample.getDoubleValue();
                        if (doubleValue > max) {
                            max = doubleValue;
                            maxIndex = index;
                        }
                        if (doubleValue < min) {
                            min = doubleValue;
                            minIndex = index;
                        }
                        if (i < samplesInAverage) {
                            lastSum += doubleValue;
                        }
                        if (i >= samplesLeftOut) {
                            firstSum += doubleValue;
                        }
                    }
                }
                final SampleState firstSample = filterHistory[(sampleNumber + ceilSamplesPerOutput - (ceilSamplesPerOutput - 1)) % ceilSamplesPerOutput];
                final SampleState lastSample =  filterHistory[sampleNumber % ceilSamplesPerOutput];
                final DateTime centerTime = firstSample != null ? new DateTime((firstSample.getTime().getMillis() + lastSample.getTime().getMillis()) >> 1) : lastSample.getTime();
                if (firstSum > lastSum) {
                    // The sample window is generally down with time - - pick the minimum
                    final SampleState minSample = filterHistory[minIndex];
                    sampleConsumer.consumeSample(sampleNumber, minSample.getSampleOpcode(), minSample.getValue(), centerTime);
                }
                else {
                    // The sample window is generally up with time - - pick the maximum
                    final SampleState maxSample = filterHistory[maxIndex];
                    sampleConsumer.consumeSample(sampleNumber, maxSample.getSampleOpcode(), maxSample.getValue(), centerTime);
                }

            }
        }
    }

    public SampleConsumer getSampleConsumer()
    {
        return sampleConsumer;
    }

    private static class SampleState {
        private final SampleOpcode sampleOpcode;
        private final Object value;
        private final double doubleValue;
        private final DateTime time;

        public SampleState(SampleOpcode sampleOpcode, Object value, double doubleValue, DateTime time) {
            this.sampleOpcode = sampleOpcode;
            this.value = value;
            this.doubleValue = doubleValue;
            this.time = time;
        }

        public SampleOpcode getSampleOpcode() {
            return sampleOpcode;
        }

        public Object getValue() {
            return value;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public DateTime getTime() {
            return time;
        }
    }
}
