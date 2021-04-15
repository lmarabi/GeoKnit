package operationsupport;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;

import org.apache.ignite.IgniteCache;

import core.Point;
import core.Rectangle;
import utility.PointDistancePair;

public class OutputUtil {

    public static void writeCachesToFile(Iterable<String> results, String filePath, IgniteCache<String, Rectangle> cache,
            Map<String, IgniteCache<Integer, ArrayList<Point>>> cacheMap) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filePath, false), "UTF-8");
            Rectangle rect = null;
            for (String p : results) {
                rect = cache.get(p);
                IgniteCache<Integer, ArrayList<Point>> pcache = cacheMap.get(p);
                ArrayList<Point> points = pcache.get(0);
                writer.write(p + " " + rect.toString() + "" + points.size() + "\n");
            }

            writer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void writePointsToFile(Iterable<Point> points, String filePath) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filePath, false), "UTF-8");
            int i = 0;
            for (Point p : points) {
                writer.write(i + "," + p.x + "," + p.y + "," + p.day + "," + p.type_task + "\n");
                i++;
            }

            writer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void writeToFile(Iterable<PointDistancePair> points, String filePath) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filePath, false), "UTF-8");
            int i = 0;
            for (PointDistancePair p : points) {
                writer.write(i + "," + p.getPoint().x + "," + p.getPoint().y + "," + p.getDistance() + "\n");
                i++;
            }

            writer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
    
    
      public static void GeoSpatial(Iterable<Point> pointw, Iterable<Point> pointst, String filePath) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filePath, false), "UTF-8");
            
            int i = 0;
            for (Point p : pointw) 
                for (Point p1 : pointst) {
                    if (p.day == p1.day && p.type_task == p1.type_task) {
                        writer.write(i + " " + "worker---> " + p.toString() + "task---> " + p1.toString() + "\n");
                        i++;
                    }
                }

            writer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
