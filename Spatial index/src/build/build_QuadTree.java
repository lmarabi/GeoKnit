/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build;

import core.Point;
import core.Rectangle;
import java.io.FileInputStream;
import indexing.QuadTree;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

// Please run this class as 
public class build_QuadTree {

    //OldQuadTree quadTree;
    QuadTree quadTree;
    Point point;

    /* public static void main(String[] args) throws IOException {

        build_QuadTree test = new build_QuadTree();
        quadTree = test.loadQuadTree();

    }
     */
    public QuadTree loadQuadTree(String path, String path_out, int c1, int c2) throws IOException {
        quadTree = new QuadTree(new Rectangle(-180, -90, 180, 90), 60);
        FileInputStream inputStream = null;
        Scanner sc = null;
        Vector<Point> sample = new Vector<>();

        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] sections = line.split(";");

                point = new Point(Double.parseDouble(sections[c1]), Double.parseDouble(sections[c2]));
                sample.add(point);
            }
            // System.out.println(sample.toString());
            quadTree.packInRectangles(sample.toArray(new Point[sample.size()]), path_out);

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

        return quadTree;
    }

}
