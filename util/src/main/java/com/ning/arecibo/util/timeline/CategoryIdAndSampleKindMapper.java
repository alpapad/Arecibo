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

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryIdAndSampleKindMapper implements ResultSetMapper<CategoryIdAndSampleKind>
{
    @Override
    public CategoryIdAndSampleKind map(final int index, final ResultSet rs, final StatementContext ctx) throws SQLException
    {
        final int eventCategoryId = rs.getInt("event_category_id");
        final String sampleKind = rs.getString("sample_kind");
        return new CategoryIdAndSampleKind(eventCategoryId, sampleKind);
    }
}
