/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class which find and counts the sheets.
 */


package com.ensibs_project.sheetcount.model;

import android.os.Environment;
import android.util.Log;
import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FindSheets {
    public static String drawContours(String file) throws Exception {

        //Loading the OpenCV core library
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat src = Imgcodecs.imread(file);
        //Converting the source image to binary
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
        //Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        Imgproc.findContours(binary, contours, hierarchey, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
        //Drawing the Contours
        Scalar color = new Scalar(0, 0, 255);
        Imgproc.drawContours(src, contours, -1, color, 2, Imgproc.LINE_8,
                hierarchey, 2, new Point() ) ;

        boolean r = Imgcodecs.imwrite(file, src);
        Log.d("Axel", "drawContours: "+r);
        return file;
    }

    public static String houghLines(String file) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat src = Imgcodecs.imread(file);
        //Converting the image to Gray
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);
        //Detecting the edges
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 80, 80*3, 3, false);
        // Changing the color of the canny
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(edges, cannyColor, Imgproc.COLOR_GRAY2BGR);
        //Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);
        for (int i = 0; i < lines.rows(); i++) {
            double[] data1 = lines.get(i, 0);
            double rho = data1[0];
            double theta = data1[1];
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a*rho;
            double y0 = b*rho;
            //Drawing lines on the image
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*(a));
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000 *(a));
            Imgproc.line(src, pt1, pt2, new Scalar(0, 0, 255), 3);
        }
        boolean r = Imgcodecs.imwrite(file, src);
        Log.d("Axel", "houghLines: "+r);
        return file;
    }
}


