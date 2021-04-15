package operations;

import core.Circle;
import core.Point;
import core.Rectangle;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import static java.lang.Double.POSITIVE_INFINITY;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import operationsupport.CacheInfo;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

public class KNNQuery {
    
    public static void knnQuery(Point poi, Rectangle s,int k, Ignite ignite, String path,CacheInfo cacheInfo) {
       Rectangle rect = new Rectangle();
       int key = 0;
        IgniteCache<String, Rectangle> cache = ignite.cache("Rectangles");
        ArrayList<String> result = new ArrayList<String>();
        boolean found = true;
        while (found && key < cacheInfo.getQCount()) {
             rect = cache.get("Cache---" + key);
            if (rect.isIntersected(s)) {
                result.add("Cache---" + key);

            } else {
                //found=false;
            }
            key++;
        }
        ArrayList<Point> foundPoints = new ArrayList<Point>();
        Circle circle; 
        for (int i = 0; i < result.size(); i++) {
            
            IgniteCache<Integer, ArrayList<Point>> pcache = cacheInfo.getCacheMap().get(result.get(i));
            ArrayList<Point> points = pcache.get(0);
            for (Point p : points) {
                if (s.contains(p.x,p.y) ) {
                    foundPoints.add(p);                    
                }
            }      
        }
        int K = 3 ;
        Point p1 = new Point();
        PriorityQueue<Double> list = new PriorityQueue<>();
         for (Point p : foundPoints) {
             double de = p.distanceTo(poi);
            list.add(de);
        }
         int Key1 = 5 ; 
         if(list.size() >= K){
            double radius = list.peek();
            circle = new Circle(p1.x , p1.y,radius*2);
          
            ArrayList<String> result_c = new ArrayList<String>();
            while (found && key < cacheInfo.getQCount()) {
             rect = cache.get("Cache---" + key);
            if (circle.isIntersected(s)) {
                result_c.add("Cache---" + key);
                System.out.println(result_c.toString());

            } else {
                //found=false;
            }
            Key1++;

         }
    }
}
}




    
