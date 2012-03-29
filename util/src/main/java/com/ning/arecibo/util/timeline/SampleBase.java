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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class SampleBase
{
    protected static final String KEY_OPCODE = "O";

    @JsonProperty(KEY_OPCODE)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    protected SampleOpcode opcode;

    public SampleBase(final SampleOpcode opcode)
    {
        this.opcode = opcode;
    }

    public SampleBase()
    {
    }

    public SampleOpcode getOpcode()
    {
        return opcode;
    }
}
