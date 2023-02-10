/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class which find and counts the sheets.
 */


package com.ensibs_project.sheetcount.model;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.FILLED;
import static org.opencv.imgproc.Imgproc.LINE_8;
import static org.opencv.imgproc.Imgproc.circle;

import static java.lang.Integer.parseInt;

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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FindSheets {

    private int amountSheets;

    public int getCount(){
        return amountSheets;
    }

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
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
        //Drawing the Contours
        Scalar color = new Scalar(0, 0, 255);
        Imgproc.drawContours(src, contours, -1, color, 2, LINE_8,
                hierarchy, 2, new Point() ) ;

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
        Imgproc.Canny(gray, edges, 40, 40*3, 3, false);
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
            Imgproc.line(edges, pt1, pt2, new Scalar(0, 0, 255), 3);
        }
        boolean r = Imgcodecs.imwrite(file, edges);
        Log.d("Axel", "houghLines: "+r);
        return file;
    }
    public static String houghLinesP(String file) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat src = Imgcodecs.imread(file);
        //Converting the image to Gray
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);
        //Detecting the edges
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 40, 40*3, 3, false);
        // Changing the color of the canny
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(edges, cannyColor, Imgproc.COLOR_GRAY2BGR);
        //Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 150);
        for (int i = 0; i < lines.cols(); i++) {
            double[] val = lines.get(0, i);
            Imgproc.line(src, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
        }
        boolean r = Imgcodecs.imwrite(file, src);
        Log.d("Axel", "houghLines: "+r);
        return file;
    }

    public static List screening(Mat src) throws Exception{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        int middle = src.cols()/2;
        double[] red =new double[3];
        red[0]=0;
        red[1]=0;
        red[2]=255;
        boolean successiveRed = false;
        src.get(0,0,red);
        List sections =  new ArrayList();
        for (int i =0; i < src.rows(); i++){
            if (src.get(i,middle)==red && !successiveRed){
                sections.add(i);
                successiveRed = true;
            }
            else{
                successiveRed = false;
            }
        }
        return sections;
    }
    public static List screeningBlack(Mat src) throws Exception{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        int middle = src.cols()/2;
        boolean successiveBlack = false;
        String black = "black";
        List sections =  new ArrayList();
        for (int i =0; i < src.rows(); i++){
            if (checkColour(i,src,black) && !successiveBlack){
                sections.add(i);
                successiveBlack = true;
            }
            else if (!checkColour(i,src,black)){
                successiveBlack = false;
            }
        }
        return sections;
    }
    public static int counting(List sections, Mat src) throws Exception{
        int amountSheet=0;
        int middle = src.cols()/2;
        int center;
        for (int i=0; i < sections.size() -1;i++){
            center = (parseInt(sections.get(i).toString())+ parseInt(sections.get(i+1).toString()))/2;
            if (checkColour(center, src, "gray")){
                amountSheet+=1;
            }
        }

        return amountSheet;
    }

    public static boolean checkColour(int rows, Mat src, String color){
        double relativeThreshold = 0.35;
        int grayThreshold = 115;
        int blackThreshold = 115;

        int middle = src.cols()/2;
        double moy = (src.get(rows,middle)[0] + src.get(rows,middle)[1] + src.get(rows,middle)[2])/3;
        double maxRelativeDifference = 0;
        for (int i=0 ; i<3 ; i++){
            double relativeDifference = ( src.get(rows,middle)[i] - moy ) / moy;
            if (relativeDifference <0){
                relativeDifference *= -1;
            }
            if (maxRelativeDifference<relativeDifference){
                maxRelativeDifference = relativeDifference;
            }
        }
        if (maxRelativeDifference < relativeThreshold && color == "black" && moy <blackThreshold){
            return true;
        }
        if (maxRelativeDifference < relativeThreshold && color == "gray" && moy >grayThreshold){
            return true;
        }
        return false;
    }

    public static Mat drawCircles(List sections, Mat src) throws Exception {

        int middle = src.cols()/2;
        int maxWidth = 200;
        int Width;
        int center;
        boolean oddCircle = false;

        for (int i=0; i < sections.size() - 1;i++){
            Width = (parseInt(sections.get(i+1).toString()) - parseInt(sections.get(i).toString()))/2;
            center = (parseInt(sections.get(i).toString()) + parseInt(sections.get(i+1).toString()))/2;
            if (Width < maxWidth && checkColour(center, src, "gray") && Width > 5){
                maxWidth = Width;
            }
        }

        for (int i=0; i < sections.size() - 1;i++){

            center = (parseInt(sections.get(i).toString())+ parseInt(sections.get(i+1).toString()))/2;
            if (checkColour(center, src, "gray")) {
                Point point = new Point((int) middle, (int) center);
                if (!oddCircle){
                    circle(src,
                            point,
                            maxWidth,
                            new Scalar(0, 0, 255),
                            FILLED,
                            LINE_8);
                    oddCircle = true;
                }
                else {
                    circle(src,
                            point,
                            maxWidth,
                            new Scalar(0, 255, 0),
                            FILLED,
                            LINE_8);
                    oddCircle = false;
                }
            }
        }
        return src;
    }

    public Mat gaussianBlur(Mat src) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Imgproc.GaussianBlur(src,src,new Size(15,15),0);
        return src;
    }

    public String processImage(String file) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat src = Imgcodecs.imread(file);
        gaussianBlur(src);
        List sections = screeningBlack(src);
        amountSheets = counting (sections,src);
        drawCircles(sections,src);
        Imgcodecs.imwrite(file, src);
        return file;
    }


}


