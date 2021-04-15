/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build;


import core.Point;
import core.Rectangle;
import indexing.RTreeGuttman;
import indexing.AbstractRTreeBBPartitioner;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import org.apache.hadoop.conf.Configuration;

public class build_Rtree {

    AbstractRTreeBBPartitioner p;
    Point point;
    RTreeGuttman r;


    public AbstractRTreeBBPartitioner RTree(String path, String path_out ,int c1, int c2) throws IOException {
        Rectangle MBR = new Rectangle(-180, -90, 180, 90);
        Vector<Point> sample = new Vector<>();
        p = new AbstractRTreeBBPartitioner.RTreeGuttmanBBPartitioner();
        FileInputStream inputStream = null;
        r = new RTreeGuttman(4, 8);
        Scanner sc = null;
        Point[] tempsArray = new Point[]{};
        BufferedWriter out = new BufferedWriter(new FileWriter(path_out));
        
        
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] sections = line.split(";");
                double lat = Double.parseDouble(sections[c1]);
                double lan = Double.parseDouble(sections[c2]);
                point = new Point(lat, lan);
                sample.add(point);

            }
            tempsArray = sample.toArray(new Point[sample.size()]);
            p.setup(new Configuration());
            p.construct(MBR, tempsArray, 80);
            //System.out.println("Too few partitions" + p.getPartitionCount());
            Set<Integer> partitions = new HashSet<Integer>();
            for (Point pt : tempsArray) {
                partitions.add(p.overlapPartition(pt));
                int partition = p.overlapPartition(pt);
                //System.out.println(pt.x + "," + pt.y + "," + partition);
            }
            //System.out.println(p.getPartitionCount() + partitions.size());
            for (int i = 0; i < p.getPartitionCount(); i++) {
                out.write(p.getPartitionAt(i).toWKT());
               // System.out.print(p.getPartitionAt(i).toWKT());
            }

            out.close();
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

        return p;
    }

}
