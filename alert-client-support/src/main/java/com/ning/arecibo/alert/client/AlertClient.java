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

package com.ning.arecibo.alert.client;

import com.sun.jersey.api.client.UniformInterfaceException;

import java.util.Map;

// TODO Consider extracting the POJO objects from alert-data-support to share them
public interface AlertClient
{
    public int createPerson(final String firstName, final String lastName, final String nickName) throws UniformInterfaceException;

    public int createGroup(final String name);

    public Map<String, Object> findPersonOrGroupById(final int id) throws UniformInterfaceException;

    public void deletePersonOrGroupById(final int id) throws UniformInterfaceException;

    public int createEmailNotificationForPersonOrGroup(final int id, final String address);

    // SMS notifications limit the body to 140 characters
    public int createSmsNotificationForPersonOrGroup(final int id, final String address);
}