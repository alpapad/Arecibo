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

public enum DecimationMode {
    PEAK_PICK,
    AVERAGE;

    public static DecimationMode fromString(final String modeString)
    {
        for (final DecimationMode decimationMode : DecimationMode.values()) {
            if (decimationMode.name().equalsIgnoreCase(modeString)) {
                return decimationMode;
            }
        }
        return null;
    }
}
