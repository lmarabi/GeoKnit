/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operations;

import core.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import operationsupport.OutputUtil;

/**
 *
 * @author DELL
 */
public class GeoSpatial {

    public static void Geo(String filePath) throws FileNotFoundException {
        Collection<Point> w = new ArrayList<>();
        Collection<Point> t = new ArrayList<>();
        Point point;
        try {
            Scanner worker = new Scanner(new File("D:\\Range_points_worker.txt"));
            Scanner task = new Scanner(new File("D:\\Range_points_task.txt"));
            while (worker.hasNextLine()) {
                String line = worker.nextLine();
                String[] sections = line.split(",");
                double x = Double.parseDouble(sections[1]);
                double y = Double.parseDouble(sections[2]);
                int time_worker = Integer.parseInt(sections[3]);
                int type_task = Integer.parseInt(sections[4]);
                point = new Point(x, y, time_worker, type_task);
                w.add(point);
            }
            while (task.hasNextLine()) {
                String line = task.nextLine();
                String[] sections = line.split(",");
                double x = Double.parseDouble(sections[1]);
                double y = Double.parseDouble(sections[2]);
                int time_worker = Integer.parseInt(sections[3]);
                int type_task = Integer.parseInt(sections[4]);
                point = new Point(x, y, time_worker, type_task);
                t.add(point);
            }
           OutputUtil.GeoSpatial(w, t, filePath);
            

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
