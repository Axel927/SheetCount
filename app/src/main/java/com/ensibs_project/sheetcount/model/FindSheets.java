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

import android.media.ExifInterface;
import android.media.Image;
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

    private int amountSheets; //amount of sheets counted

    //getCount() returns the number of sheets that have been counted

    public int getCount(){
        return amountSheets;
    }

    //screeningBlack checks the middle of the picture and where there are black lines

    public static List screeningBlack(Mat src) throws Exception{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); //give access to opencv
        int middle = src.cols()/2; //middle of the image
        boolean successiveBlack = false; //True if the previous detected colour was black else false
        int previousLine = 0;
        String black = "black";
        List sections =  new ArrayList(); //List containing the position of the black lines
        for (int i =0; i < src.rows(); i++){ //for each pixel in the middle of the image
            if (checkColour(i,src,black) && !successiveBlack){ //if the pixel is black and the previous pixel was not black
                if (previousLine != 0) {
                    sections.add(previousLine);
                    sections.add(i); //add the position to the list
                }
                successiveBlack = true; //the previous pixel is now remembered as black
                previousLine = i;
            }
            else if (!checkColour(i,src,black)){ //if the pixel is not black
                successiveBlack = false; //the previous pixel is no longer remembered as black
            }
        }
        return sections; //return the list
    }

    public static List screeningGray(List sections,Mat src) throws Exception{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); //give access to opencv
        int center; //center of two black lines
        for (int i =0; i < sections.size()/2; i++){ //for each pixel in the middle of the image
            center = (parseInt(sections.get(2*i).toString())+ parseInt(sections.get(2*i+1).toString()))/2; //determine the center of the two lines
            if (!checkColour(center,src,"gray")) {
                sections.remove(2*i+1);
                sections.remove(2*i);
            }
        }
        return sections; //return the list
    }

    //counting counts the amount of sheets among the list of suspected positions

    public static int counting(List sections, Mat src) throws Exception{
        int amountSheet=sections.size()/2; //reinitialize the amount of sheets

        return amountSheet; //return the amount of sheets
    }

    //checkColour checks if a pixel is gray, black or another color

    public static boolean checkColour(int rows, Mat src, String color){
        double relativeThreshold = 0.20; //allowed relative difference between the RGB values of a pixel and their average
        int grayThreshold = 140; //threshold under which a pixel is considered gray
        int blackThreshold = 115; //threshold over which a pixel is considered black

        int middle = src.cols()/2; //middle of the image
        double moy = (src.get(rows,middle)[0] + src.get(rows,middle)[1] + src.get(rows,middle)[2])/3; //average of the pixels RGB values
        double maxRelativeDifference = 0; //maximum relative difference found
        for (int i=0 ; i<3 ; i++){ //for each colour
            double relativeDifference = ( src.get(rows,middle)[i] - moy ) / moy; //determine the relative difference
            if (relativeDifference <0){ //if it's negative
                relativeDifference *= -1; //multiply it by -1
            }
            if (maxRelativeDifference<relativeDifference){ //if it's superior to the previous maximum
                maxRelativeDifference = relativeDifference; //it's the new maximum
            }
        }
        if (maxRelativeDifference < relativeThreshold && color == "black" && moy <blackThreshold){ //if you are looking for black and the thresholds for black are met
            return true;
        }
        if (maxRelativeDifference < relativeThreshold && color == "gray" && moy >grayThreshold){ //if you are looking for gray and the thresholds for gray are met
            return true;
        }
        return false;
    }

    //drawCircles draw circles on the counted sheets

    public static Mat drawCircles(List sections, Mat src) throws Exception {

        int middle = src.cols()/2; //middle of the image
        int maxWidth = 200; //maximum size of a circle
        int width; //width of a circle
        int center; //position of a circle
        boolean oddCircle = false; //if it is a an odd or even numbered circle to choose the colour

        for (int i=0; i < sections.size()/2;i++){ //for each two successive lines
            width = (parseInt(sections.get(2*i+1).toString()) - parseInt(sections.get(2*i).toString()))/2; //determine the width
            Log.d("Leon", "drawCircles: "+ Integer.toString(width));
            center = (parseInt(sections.get(2*i).toString()) + parseInt(sections.get(2*i+1).toString()))/2; //and position
            if (width < maxWidth && width > 5){ //if the width is inferior to the maxWidth and inferior to the minWidth
                maxWidth = width; //Width becomes maxWidth
            }
        }
        Log.d("Leon", "drawCircles: "+ Integer.toString(maxWidth));
        for (int i=0; i < sections.size()/2;i++){ //for each two successive lines

            center = (parseInt(sections.get(2*i).toString())+ parseInt(sections.get(2*i+1).toString()))/2; //determine the center
            Point point = new Point((int) middle, (int) center); //coordinate of the center
            if (!oddCircle){ //if it's an even circle
                circle(src, //create a red circle
                        point,
                        maxWidth,
                        new Scalar(0, 0, 255),
                        FILLED,
                        LINE_8);
                oddCircle = true; //the next circle is odd
                }
            else { //if it's an odd circle
                circle(src, //create a green circle
                        point,
                        maxWidth,
                        new Scalar(0, 255, 0),
                        FILLED,
                        LINE_8);
                oddCircle = false; //the next circle is even
                }
            }
        return src; //return the image with the circles
    }

    

    //gaussianBlur blurs the image

    public Mat gaussianBlur(Mat src) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); //load the opencv library
        Imgproc.GaussianBlur(src,src,new Size(15,15),0); //use the gaussian blur while taking into account the pixel in a 15*15 area around each pixel
        return src;
    }

    public Mat medianBlur (Mat src) throws Exception{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); //load the opencv library
        Imgproc.medianBlur(src,src,5); //use the gaussian blur while taking into account the pixel in a 15*15 area around each pixel
        return src;
    }

    public String processImage(String file) throws Exception {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); //load the opencv library
        Mat src = Imgcodecs.imread(file); //get  the image
        ExifInterface exif = new ExifInterface(file);
     

        //medianBlur(src); //blur it
        List sections = screeningBlack(src); //find the sections
        Log.d("Leon", "processImage: " + sections);
        sections = screeningGray(sections,src);
        Log.d("Leon", "processImage: " + sections);
        amountSheets = counting (sections,src); //Determine the amount of sheets
        drawCircles(sections,src); //Draw circles on the sheets
        Imgcodecs.imwrite(file, src); //transform the image into a file
        return file; //return the file
    }


}


