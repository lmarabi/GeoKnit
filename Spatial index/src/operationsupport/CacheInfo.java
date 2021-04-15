package operationsupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.ignite.IgniteCache;

import core.Point;
import core.Rectangle;

public class CacheInfo {
	
 	 IgniteCache<Integer, File> fcache;
 	 IgniteCache<String, Rectangle> ccache;
 	 int qCount = 0;
 	Map <String,IgniteCache<Integer, ArrayList<Point>>> cacheMap;
 	
 	public void setFCache( IgniteCache<Integer, File> fcache){
 		this.fcache = fcache;
 	}
 	
 	public IgniteCache<Integer, File> getFCache(){
 		return fcache;
 	}
 	
 	public void setCCache(IgniteCache<String, Rectangle>  ccache){
 		this.ccache=ccache;
 	}
 	
 	public IgniteCache<String, Rectangle> getCCache(){
 		return ccache;
 	}
 	
 	public void setQCount(int qCount) {
 		this.qCount=qCount;
 	}
 	public int getQCount() {
 		return qCount;
 	}
 	
 	
 	public void setCacheMap(Map <String,IgniteCache<Integer, ArrayList<Point>>> cacheMap){
 		this.cacheMap=cacheMap;
 	}
 	
 	public Map <String,IgniteCache<Integer, ArrayList<Point>>> getCacheMap(){
 		return cacheMap;
 	}

}
