import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.configuration.FileSystemConfiguration;
import org.apache.ignite.igfs.IgfsInputStream;
import org.apache.ignite.igfs.IgfsPath;
import sa.edu.uqu.geoknit.dataTypesOsa.City;
//import jdk.nashorn.internal.pars  ccxvvx'lk;\gjhASDF
// ]
//
// er.JSONParser;
import sa.edu.uqu.geoknit.dataTypesOsa.Point;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IgniteHelloWorld1 {
    @SuppressWarnings("unchecked")


    public static void main(String[] args) throws ParseException {
        // Preparing IgniteConfiguration using Java APIs
        IgniteConfiguration cfg = new IgniteConfiguration();
        FileSystemConfiguration fileSystemCfg = new FileSystemConfiguration();
        fileSystemCfg.setName("myFileSystem");


        cfg.setFileSystemConfiguration(fileSystemCfg);

        // The node will be started as a client node.
        cfg.setClientMode(true);

        // Classes of custom Java logic will be transferred over the wire from this app.
        cfg.setPeerClassLoadingEnabled(true);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));


        // Starting the node
        Ignite ignite = Ignition.start(cfg);

        Ignite ignitefs = Ignition.ignite();
        IgniteFileSystem fs = ignitefs.fileSystem("myFileSystem");

// Create directory.
        IgfsPath dir = new IgfsPath("/myDir");

        fs.mkdirs(dir);

// Create file and write some data to it.
        IgfsPath file = new IgfsPath( dir , "cities-boundaries.json");

// Read from file.
        try (IgfsInputStream in = fs.open(file)) {
            int f =  in.read();
            System.out.println("hhhhhh "+f );
        } catch (IOException e) {
            e.printStackTrace( );
        }

// Delete directory.
        fs.delete(dir, true);

        // Create an IgniteCache and put some values in it.
        IgniteCache<Point, City> cache = ignite.getOrCreateCache("myCache");
//        cache.put(1, "Hello");


        System.out.println(">> Created the cache and add the values.");

        // Executing custom Java compute task on server nodes.
        ignite.compute(ignite.cluster().forServers()).broadcast(new RemoteTask());

        System.out.println(">> Compute task is executed, check for output on the server nodes.");

        // Disconnect from the cluster.
        ignite.close();
//

    }

    /**
     * A compute tasks that prints out a node ID and some details about its OS and JRE.
     * Plus, the code shows how to access data stored in a cache from the compute task.
     */
    private static class RemoteTask implements IgniteRunnable {
        @IgniteInstanceResource
        Ignite ignite;

        public void run() {
            System.out.println(">> Executing the compute task");

            System.out.println(
                    "   Node ID: " + ignite.cluster().localNode().id() + "\n" +
                            "   OS: " + System.getProperty("os.name") +
                            "   JRE: " + System.getProperty("java.runtime.name"));

            IgniteCache<Point, City> cache = ignite.cache("myCache");

//            System.out.println(">> " + cache.get(1) + " " + cache.get(2) +" "+ cache.get(3));

//            for ( Point key : cache.getAll() ){
//                system.out.println(cache.get(key))
//            }
            Iterator iter = cache.iterator();
//
            while( iter.hasNext() ){

                Map.Entry ent = (Map.Entry) iter.next();
                System.out.println(ent.getKey().toString() );
            }

//            cache.forEach(
//                    c -> {
//                        if (c.getValue( ).toString( ) != null) {
//                            System.out.println(c.getValue( ).toString( ));
//                        }
//                    });
        }
    }

    public static void getCity(JSONObject cityObjevct , IgniteCache<Point, City> cache){
        City newCity = new City();

        Long city_id = (Long) cityObjevct.get("city_id");
        newCity.setCity_id(city_id);

        Long region_id = (Long) cityObjevct.get("region_id");
        newCity.setRegion_id(region_id);

        String name_ar = (String) cityObjevct.get("name_ar");
        newCity.setName_ar(name_ar);

        String name_en = (String) cityObjevct.get("name_en");
        newCity.setName_en(name_en);

        JSONArray centerObj = (JSONArray) cityObjevct.get("center");
        String point = (String) centerObj.toString();
        newCity.setCenter( new Point( point ));

        JSONArray boundariesArr = (JSONArray) cityObjevct.get("boundaries");
//        System.out.println("city" +name_ar);
        if(boundariesArr !=null){
            newCity.setBoundaries( boundariesArr.toString()) ;
        }

        cache.put(newCity.center, newCity);


//        System.out.println(boundariesArr );

//            boundariesArr.forEach(b -> System.out.println( b.toString() ) );
//        System.out.println("cityObject: "+cityObject  );
//        String[] centerArr = center.split(",");
//        System.out.println("center :"+ centerArr[0]);

//        return new City();
    }
    private String[] toStringArray( JSONArray jsonArray){

        return new String[6];
    }

}
