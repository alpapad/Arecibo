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

package com.ning.arecibo.alert;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.ning.arecibo.alert.conf.ConfigManager;
import com.ning.arecibo.alert.confdata.guice.AlertDataModule;
import com.ning.arecibo.alert.guice.AlertServiceConfig;
import com.ning.arecibo.alert.guice.AlertServiceModule;
import com.ning.arecibo.alert.guice.SelfUUID;
import com.ning.arecibo.alert.manage.AlertEventProcessor;
import com.ning.arecibo.client.AggregatorClientModule;
import com.ning.arecibo.event.receiver.RESTEventReceiverModule;
import com.ning.arecibo.event.receiver.UDPEventReceiverModule;
import com.ning.arecibo.event.transport.EventService;
import com.ning.arecibo.util.EmbeddedJettyConfig;
import com.ning.arecibo.util.EmbeddedJettyJerseyModule;
import com.ning.arecibo.util.Logger;
import com.ning.arecibo.util.lifecycle.Lifecycle;
import com.ning.arecibo.util.lifecycle.LifecycleEvent;
import com.ning.arecibo.util.lifecycle.LifecycleModule;
import com.ning.arecibo.util.service.ServiceDescriptor;
import com.ning.arecibo.util.service.ServiceLocator;
import org.eclipse.jetty.server.Server;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AreciboAlertService
{
    private static final Logger log = Logger.getLogger(AreciboAlertService.class);

    private final Lifecycle lifecycle;
    private final Server server;
    private final ServiceLocator serviceLocator;
    private final AlertServiceConfig alertServiceConfig;
    private final EmbeddedJettyConfig jettyConfig;
    private final Integer udpPort;
    private final UUID selfUUID;
    private final ConfigManager confStatusManager;

    @Inject
    private AreciboAlertService(final Lifecycle lifecycle,
                                final Server server,
                                final ServiceLocator serviceLocator,
                                final AlertServiceConfig alertServiceConfig,
                                final EmbeddedJettyConfig jettyConfig,
                                @SelfUUID final UUID selfUUID,
                                @Named("UDPServerPort") final int udpPort,
                                final ConfigManager confStatusManager)
    {
        this.lifecycle = lifecycle;
        this.server = server;
        this.serviceLocator = serviceLocator;
        this.alertServiceConfig = alertServiceConfig;
        this.jettyConfig = jettyConfig;
        this.udpPort = udpPort;
        this.selfUUID = selfUUID;
        this.confStatusManager = confStatusManager;
    }

    private void run()
    {
        final long startTime = System.currentTimeMillis();
        log.info("Starting up Alert Service on port %d", jettyConfig.getPort());

        serviceLocator.startReadOnly();

        // Start the confStatusManager
        confStatusManager.start();

        // Advertise alert endpoints
        final Map<String, String> map = new HashMap<String, String>();
        map.put("host", jettyConfig.getHost());
        map.put("port", String.valueOf(jettyConfig.getPort()));
        map.put(EventService.JETTY_PORT, String.valueOf(jettyConfig.getPort()));
        map.put(EventService.UDP_PORT, String.valueOf(udpPort));
        final ServiceDescriptor self = new ServiceDescriptor(selfUUID, alertServiceConfig.getServiceName(), map);
        serviceLocator.advertiseLocalService(self);

        try {
            lifecycle.fire(LifecycleEvent.START);
            server.start();
        }
        catch (Exception ex) {
            log.error(ex);
            return;
        }

        final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
        log.info("STARTUP COMPLETE: server started in %d:%02d", secondsToStart / 60, secondsToStart % 60);

        final Thread t = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                t.interrupt();
            }
        });

        try {
            Thread.currentThread().join();
        }
        catch (InterruptedException ex) {
            // continue;
        }

        try {
            log.info("Shutting Down Alert Service");
            serviceLocator.stop();

            log.info("Stopping configStatusManager");
            confStatusManager.stop();

            log.info("Stopping lifecycle manager");
            lifecycle.fire(LifecycleEvent.STOP);

            log.info("Stopping jetty server");
            server.stop();

            // never gets here for some reason
            log.info("Shutdown completed");
        }
        catch (Exception e) {
            log.warn(e);
        }
    }

    public static void main(final String[] args)
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
            new LifecycleModule(),
            new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                }
            },
            new AlertDataModule(),
            new EmbeddedJettyJerseyModule("/xn/rest/.*", ImmutableList.<String>of("com.ning.arecibo.event.receiver", "com.ning.arecibo.util.jaxrs")),
            new RESTEventReceiverModule(AlertEventProcessor.class, "arecibo.alert:name=AlertEventProcessor"),
            new UDPEventReceiverModule(),
            new AggregatorClientModule(),
            new AlertServiceModule());

        final AreciboAlertService service = injector.getInstance(AreciboAlertService.class);
        try {
            service.run();
        }
        catch (Exception e) {
            log.error(e, "Unable to start. Exiting.");
            System.exit(-1);
        }
    }
}
