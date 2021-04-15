package operations;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

import core.Point;
import core.Rectangle;
import operationsupport.CacheInfo;
import operationsupport.OutputUtil;

public class RangeQuery {

    public static void rangeQuery(Rectangle s, Ignite ignite, String pathCache, String pathPoint, CacheInfo cacheInfo) throws FileNotFoundException {

        //File file = new File("D:\\RangeQueryOutput.txcacheInfot");
        //Instantiating the PrintStream class
        //PrintStream stream = new PrintStream(file);
        int key = 0;
        IgniteCache<String, Rectangle> cache = ignite.cache("Rectangles");
        ArrayList<String> result = new ArrayList<String>();
        boolean found = true;
        while (found && key < cacheInfo.getQCount()) {
            Rectangle rect = cache.get("Cache---" + key);
            if (rect.isIntersected(s)) {
                result.add("Cache---" + key);

            } else {
                //found=false;
            }
            key++;
        }
        ArrayList<Point> foundPoints = new ArrayList<Point>();
        
        for (int i = 0; i < result.size(); i++) {
            
            IgniteCache<Integer, ArrayList<Point>> pcache = cacheInfo.getCacheMap().get(result.get(i));
            ArrayList<Point> points = pcache.get(0);
            for (Point p : points) {
                if (s.contains(p.x,p.y) ) {
                    foundPoints.add(p);                    
                }
            }      
        }
        
       
        OutputUtil.writeCachesToFile(result, pathCache, cache, cacheInfo.getCacheMap());
        OutputUtil.writePointsToFile(foundPoints, pathPoint);
    }

// some time passes
}
