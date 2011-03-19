package com.ning.arecibo.collector.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.ning.arecibo.collector.ResolutionUtils;
import com.ning.arecibo.collector.config.CollectorConfig;
import com.ning.arecibo.collector.dao.CollectorDAO;
import com.ning.arecibo.util.Logger;
import com.ning.arecibo.util.jdbi.DBIProvider;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;

public class CollectorModule extends AbstractModule
{

    final static Logger log = Logger.getLogger(CollectorModule.class);
    private final CollectorConfig config;

    public CollectorModule(CollectorConfig config)
    {
        this.config = config;
    }

    @Override
    public void configure()
    {
        // set up db connection, with named statistics
        final Named moduleName = Names.named(CollectorConstants.COLLECTOR_DB);

        bind(DBI.class).annotatedWith(moduleName).toProvider(new DBIProvider(
            config.getJdbcUrl(),
            config.getDBUsername(),
            config.getDBPassword(),
            config.getMinConnectionsPerPartition(),
            config.getMaxConnectionsPerPartition()
        )).asEagerSingleton();
        bind(IDBI.class).annotatedWith(moduleName).to(Key.get(DBI.class, moduleName));
        bind(CollectorDAO.class).asEagerSingleton();

        bind(ResolutionUtils.class).toInstance(new ResolutionUtils());

        ExportBuilder builder = MBeanModule.newExporter(binder());

        builder.export(CollectorDAO.class).as("arecibo.collector:name=CollectorDAO");
    }
}

