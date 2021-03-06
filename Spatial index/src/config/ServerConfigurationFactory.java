package config;

import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.sharedfs.TcpDiscoverySharedFsIpFinder;
//import org.gridgain.grid.configuration.GridGainConfiguration;
//import org.gridgain.grid.configuration.SnapshotConfiguration;
//import org.gridgain.grid.internal.processors.cache.database.snapshot.CompressionOption;

/** This file was generated by Ignite Web Console (02/11/2021, 13:17) **/
public class ServerConfigurationFactory {
    /**
     * Configure grid.
     * 
     * @return Ignite configuration.
     * @throws Exception If failed to construct Ignite configuration instance.
     **/
    public static IgniteConfiguration createConfiguration() throws Exception {
        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setIgniteInstanceName("Cluster-3");

        TcpDiscoverySpi discovery = new TcpDiscoverySpi();

        discovery.setIpFinder(new TcpDiscoverySharedFsIpFinder());

        cfg.setDiscoverySpi(discovery);

        DataStorageConfiguration dataStorageCfg = new DataStorageConfiguration();

        DataRegionConfiguration dataRegionCfg = new DataRegionConfiguration();

        dataRegionCfg.setInitialSize(1073741824L);
        dataRegionCfg.setMaxSize(21474836480L);

        dataStorageCfg.setDefaultDataRegionConfiguration(dataRegionCfg);

        cfg.setDataStorageConfiguration(dataStorageCfg);

        cfg.setWorkDirectory("C://home/ignite/files");
        cfg.setIgniteHome("C://home/ignite");

        //GridGainConfiguration plugin = new GridGainConfiguration();

        //plugin.setRollingUpdatesEnabled(true);

        //SnapshotConfiguration snapshotCfg = new SnapshotConfiguration();

        //snapshotCfg.setSnapshotsPath("C://home/ignite/files");
        //snapshotCfg.setCompressionOption(CompressionOption.ZIP);

        //plugin.setSnapshotConfiguration(snapshotCfg);

        //cfg.setPluginConfigurations(plugin);

        return cfg;
    }
}