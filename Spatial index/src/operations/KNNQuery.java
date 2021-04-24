package operations;

import core.Circle;
import core.Point;
import core.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import operationsupport.CacheInfo;
import operationsupport.OutputUtil;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

public class KNNQuery {

    static Rectangle MBR ;;
    static Rectangle rect;
    public static void knnQuery(Point poi, int t, Rectangle s, int k, Ignite ignite, String path, CacheInfo cacheInfo) {
        List<Point> point = new ArrayList<>();
        
        
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
        
        List<Circle> list = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            IgniteCache<Integer, ArrayList<Point>> pcache = cacheInfo.getCacheMap().get(result.get(i));
            ArrayList<Point> points = pcache.get(0);
            for (Point p : points) {
                if (s.contains(p.x, p.y)) {
                    //&& p.day == 162  && p.type_task == 11
                double distance = p.distanceTo(poi);
                Circle circle = new Circle(p.x,p.y,distance*2);
                list.add(circle);
                Collections.sort(list, (a, b) -> b.compareTo(a));
                Collections.reverse(list);
                
                }
                point.add(p);
                
            }
        }
       // System.out.println(list.toString());



    ArrayList<String> result_c = new ArrayList<String>();
    for(int i = 3 ; i == k ; i++){
       // if(i == k){
          Circle circle = list.get(i);
    //for(Circle circle : list.toArray(new Circle[list.indexOf(k)])){ 
    
         MBR = circle.getMBR();
        int key1 = 0;
        while (found && key1 < cacheInfo.getQCount()) { 
            Rectangle getMBR = cache.get("Cache---" + key1);
            if (MBR.isIntersected(getMBR)) {
                //double distance = p.distanceTo(poi);
                result_c.add("Cache---" + key1);   
            }                    
            key1++;
        }
    }
   // }
     //System.out.println(result_c.toString());
     
    ArrayList <String> knn = new ArrayList<>(); 
          for (int i = 0; i < result_c.size(); i++) {
            IgniteCache<Integer, ArrayList<Point>> pcache = cacheInfo.getCacheMap().get(result_c.get(i));
            ArrayList<Point> points = pcache.get(0);
            for (Point p : points) {
                if (MBR.contains(p)) {
                    //&& p.day == 162  && p.type_task == 11
                double distance = p.distanceTo(poi);
                Collections.sort(knn);  
                knn.add(result_c.get(i) + "  distance: "+distance + " km" + "\n");

                }  
            }
        }

          System.out.println(knn.toString());
          OutputUtil.knnquary(knn ,path, poi, cache, cacheInfo.getCacheMap());

    }
}


