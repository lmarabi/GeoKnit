package operationsupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

import core.Point;
import core.Rectangle;

public class SetUp {
	
	public static void loadFiles(Ignite ignite, String path, int key, CacheInfo cacheInfo) {

		IgniteCache<Integer, File> fcache = ignite.getOrCreateCache("FileCache");
 		File f = new File(path);
 		fcache.put(key, f);
 		cacheInfo.setFCache(fcache);
 	}
 	
 	
 	public static int countLines(CacheInfo cacheInfo) throws IOException {
 	    LineNumberReader reader  = new LineNumberReader(new FileReader(cacheInfo.getFCache().get(0)));
 	    int cnt = 0;
 	    String lineRead = "";
 	    while ((lineRead = reader.readLine()) != null) {}

 	    cnt = reader.getLineNumber(); 
 	    reader.close();
 	    return cnt;
 	}
 	
 	
 	public static void createCachesFromFile(Ignite ignite, CacheInfo cacheInfo) throws IOException {

 		 Map <String,IgniteCache<Integer, ArrayList<Point>>> cacheMap = new HashMap <String,IgniteCache<Integer, ArrayList<Point>>>();
 		 
 		 BufferedReader br = new BufferedReader(new FileReader(cacheInfo.getFCache().get(0)));
 		 IgniteCache<String, Rectangle> ccache = ignite.getOrCreateCache("Rectangles");
		 int qCount = countLines(cacheInfo);
 		 String line = null;
 		 while ((line = br.readLine()) != null) {
 			 
 		   String[] parts = line.split(",");
 		   int next = Integer.parseInt(parts[0]);
 		   IgniteCache<Integer, ArrayList<Point>> pcache = ignite.getOrCreateCache("Cache---"+next);
 		   cacheMap.put("Cache---"+next, pcache);
 		   ccache.put(pcache.getName(), new Rectangle(Double.parseDouble(parts[1]),Double.parseDouble(parts[2]),
 				   Double.parseDouble(parts[3]),Double.parseDouble(parts[4])));

 		 }
 		 br.close();
 		 cacheInfo.setCCache(ccache);
 		 cacheInfo.setQCount(qCount);
 		 cacheInfo.setCacheMap(cacheMap);
 		 
 	}
 	public static void insert(Ignite ignite,CacheInfo cacheInfo,int c1, int c2, int c3, int c4)
 			throws ParseException, NumberFormatException, IOException {
 		
 		
 		String line = null;
 		for(int i=0;i<cacheInfo.getQCount();i++) {
 			BufferedReader br = new BufferedReader(new FileReader(cacheInfo.getFCache().get(1)));
 			Rectangle rect = cacheInfo.getCCache().get("Cache---"+i);
 			ArrayList<Point> p = new ArrayList<Point>();

 			 while ((line = br.readLine()) != null) {
 			   String[] parts = line.split(";");
 			   double x = Double.parseDouble(parts[c1]);
 			   double y = Double.parseDouble(parts[c2]);
                           int day = Integer.parseInt(parts[c3]);
                           int type_task = Integer.parseInt(parts[c4]);
 			   Point point = new Point(x,y);
                           Point pointD = new Point(x,y,day,type_task);
 			   if(rect.contains(point)) {
 				   p.add(pointD);
 			   	}
 			   }
 			 ignite.cache("Cache---"+i).put(0, p);
 			 br.close();
 		}


 	}

 	@SuppressWarnings("unchecked")
 	public static void printCahces(Ignite ignite, CacheInfo cacheInfo,String cach ) throws IOException {
 		int s=0;
 		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(cach, false), "UTF-8");
 		for (int key = 0; key < cacheInfo.getQCount(); key++) {
 			 String name = ignite.cache("Cache---"+key).getName();
 			 ArrayList<Point> p = ((ArrayList<Point>) ignite.cache(name).get(0));
 			 if(p!=null)
 				 s=s+p.size(); 
 			 //System.out.println("" + key + "," +p.size()+"");
 			writer.write(key+","+p.size()+"\n");

 			 
 		}
 		writer.close();
 		
 	}


}
