/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build;

import indexing.GridPartitioner;
import core.Point;
import core.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 *
 * @author DELL
 */
public class build_GridFile {

    GridPartitioner GridFile;
    Point point;

  /*  public static void main(String[] args) throws IOException {
        build_GridFile Grid = new build_GridFile();
        Grid.GridFile();
    }
*/
    Scanner s;

    public GridPartitioner GridFile(String path,String path_out, int c1, int c2) throws IOException {
        Rectangle Mbr = new Rectangle(-180, -90, 180, 90);
        GridFile = new GridPartitioner();
        Vector<Point> sample = new Vector<>();
        BufferedWriter out = new BufferedWriter(new FileWriter(path_out));

        try {
            s = new Scanner(new File(path));
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] sections = line.split(";");
                point = new Point(Double.parseDouble(sections[c1]), Double.parseDouble(sections[c2]));

                sample.add(point.getMBR().getCenterPoint());

            }
            GridFile.createFromPoints(Mbr, sample.toArray(new Point[sample.size()]), 50);
            //System.out.println("x,y,partition");
            for(Point p1 : sample){
                int partition = GridFile.overlapPartition(p1);
                //System.out.println(p1.x + "," + p1.y + "," + partition);
            }
            //System.out.println("Partition count " + GridFile.getPartitionCount());
            for (int i = 0; i < GridFile.getPartitionCount(); i++) {
                out.write(GridFile.getPartitionAt(i).toWKT());
                System.out.print(GridFile.getPartitionAt(i).toWKT()); 
                
            }
            
            out.close();

            s.close();

            //map.put(1, quadTree);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return GridFile;
    }

   

}
