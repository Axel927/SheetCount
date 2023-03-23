/**
 * This file contains the class which find and counts the sheets.
 * Copyright © 2023  Collen Leon and Tremaudant Axel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 */

/*

  To start VariableThreshold will for every gray threshold between 50 and 200 in steps of 5 find the black lines and use them to create a list of the top and bottom of suspected sheets (screeningBlack),
  he will then check if the middle pixel of each suspected sheet is gray if it isn't that sheet will be removed from the list (screeningGray), then he will check for each suspected sheet if it is near the median height otherwise it is removed from the list (checkHeight).
  Finally it checks if the amount of detected sheets is higher than the previous maximum if it is the threshold is saved as the new maximum.

  Once the optimal threshold has been determined we repeat the operation once more using the optimal threshold then we draw a dot on each detected sheet and show the image (processImage)
 */



package com.ensibs_project.sheetcount.model;

import static org.opencv.imgproc.Imgproc.FILLED;
import static org.opencv.imgproc.Imgproc.LINE_8;
import static org.opencv.imgproc.Imgproc.circle;
import static java.lang.Integer.parseInt;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FindSheets {

    private int amountSheets = 0;                                                   //amount of sheets counted
    private int threshold = 140;

    /**
     * getCount returns the amount of detected sheets
     * @return amount of detected sheets
     */
    public int getCount(){
        return amountSheets;
    }

    /**
     * getThreshold returns the current threshold
     * @return returns the current threshold
     */
    public int getThreshold() { return threshold;}

    /**
     * changeThreshold changes the current threshold
     * @param newThreshold the wanted new threshold
     */
    public void changeThreshold(int newThreshold){ threshold = newThreshold;}

    /**
     * screeningBlack checks the middle of the picture and where there are black lines
     * @param sections List of detected sheets
     * @param src Image of the sheets
     */
    public void screeningBlack(List<Integer> sections,Mat src) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );                         //give access to opencv
        boolean successiveBlack = false;                                        //True if the previous detected colour was black else false
        int previousLine = 0;                                                   //height of the previous detected line
        String black = "black";
        for (int i =0; i < src.rows(); i++){                                    //for each pixel in the middle of the image
            if (checkColour(i,src,black) && !successiveBlack){                  //if the pixel is black and the previous pixel was not black
                if (previousLine != 0) {                                        //if it is not the first detected line
                    sections.add(previousLine);                                 //add the bottom of the sheet to the list
                    sections.add(i);                                            //add the top of the sheet to the list
                }
                successiveBlack = true;                                         //the previous pixel is now remembered as black
                previousLine = i;                                               //the new line becomes the previous line
            }
            else if (!checkColour(i,src,black)){                                //if the pixel is not black
                successiveBlack = false;                                        //the previous pixel is no longer remembered as black
            }
        }
    }

    /**
     * screeningGray checks the middle of the picture and detects where there are black lines
     * @param sections List of detected sheets
     * @param src Image of the sheets
     */
    public void screeningGray(List<Integer> sections,Mat src) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );                         //give access to opencv
        int center; //center of two black lines
        for (int i =0; i < sections.size()/2; i++){                             //for each pixel in the middle of the image
            center = (parseInt(sections.get(2*i).toString())+ parseInt(sections.get(2*i+1).toString()))/2; //determine the center of the two lines
            if (!checkColour(center,src,"gray")) {                        //if the pixel is not gray
                sections.remove(2*i+1);                                      //remove the top of the sheet from the list
                sections.remove(2*i);                                        //remove the bottom of the sheet from the list
            }
        }
    }


    /**
     * counting counts the amount of detected sheets
     * @param sections List of detected sheets
     * @return amount of detected sheets
     */
    public static int counting(List<Integer> sections) {
        return sections.size()/2;                                               //return the amount of sheets
    }

    /**
     * checkColour verify if a pixel of the image is gray, black or another color
     * @param rows which lines colour needs to be checked
     * @param src Image of the sheets
     * @param color which colour is looked for
     * @return Boolean whether the color is the asked one or not
     */
    public boolean checkColour(int rows, Mat src, String color){
        double relativeThreshold = 0.2;                                        //allowed relative difference between the RGB values of a pixel and their average
        int grayThreshold = threshold;                                                //threshold under which a pixel is considered gray
        int blackThreshold = threshold * 110 / 140;                                               //threshold over which a pixel is considered black

        int middle = src.cols()/2;                                              //middle of the image
        double moy = (src.get(rows,middle)[0] + src.get(rows,middle)[1] + src.get(rows,middle)[2])/3; //average of the pixels RGB values
        double maxRelativeDifference = 0;                                       //maximum relative difference found
        for (int i=0 ; i<3 ; i++){                                              //for each colour
            double relativeDifference = ( src.get(rows,middle)[i] - moy ) / moy;//determine the relative difference
            if (relativeDifference <0){                                         //if it's negative
                relativeDifference *= -1;                                       //multiply it by -1
            }
            if (maxRelativeDifference<relativeDifference){                      //if it's superior to the previous maximum
                maxRelativeDifference = relativeDifference;                     //it's the new maximum
            }
        }
        if (maxRelativeDifference < relativeThreshold && Objects.equals(color, "black") && moy <blackThreshold){ //if you are looking for black and the thresholds for black are met
            return true;
        }
        //if you are looking for gray and the thresholds for gray are met
        return maxRelativeDifference < relativeThreshold && Objects.equals(color, "gray") && moy > grayThreshold;
    }

    /**
     * drawCircles draw circles on the detected sheets
     * @param sections List of detected sheets
     * @param src Image of the sheets
     */
    public static void drawCircles(List<Integer> sections, Mat src) {

        int middle = src.cols()/2;                                              //middle of the image
        int maxWidth = 200;                                                     //maximum size of a circle
        int width;                                                              //width of a circle
        int center;                                                             //position of a circle
        boolean oddCircle = false;                                              //if it is a an odd or even numbered circle to choose the colour

        for (int i=0; i < sections.size()/2;i++){                               //for each two successive lines
            width = (parseInt(sections.get(2*i+1).toString()) - parseInt(sections.get(2*i).toString()))/2; //determine the width
            Log.d("Leon", "drawCircles: "+ width);
            if (width < maxWidth && width > 5){                                 //if the width is inferior to the maxWidth and inferior to the minWidth
                maxWidth = width;                                               //Width becomes maxWidth
            }
        }
        if (maxWidth == 200){
            maxWidth = 5;
        }
        Log.d("Leon", "drawCircles: "+ maxWidth);
        for (int i=0; i < sections.size()/2;i++){                               //for each two successive lines

            center = (parseInt(sections.get(2*i).toString())+ parseInt(sections.get(2*i+1).toString()))/2; //determine the center
            Point point = new Point(middle, center);                            //coordinate of the center
            if (!oddCircle){                                                    //if it's an even circle
                circle(src,                                                     //create a red circle
                        point,
                        maxWidth,
                        new Scalar(0, 0, 255),
                        FILLED,
                        LINE_8);
                oddCircle = true;                                               //the next circle is odd
                }
            else {                                                              //if it's an odd circle
                circle(src,                                                     //create a green circle
                        point,
                        maxWidth,
                        new Scalar(0, 255, 0),
                        FILLED,
                        LINE_8);
                oddCircle = false;                                              //the next circle is even
                }
            }
    }

    /**
     * checkHeight remove all detected positions whose height is to different from the median
     * @param sections List of detected sheets
     */
    public void checkHeight(List<Integer> sections) {
        double threshold= 0.5;                                                  //Accepted variation from the median
        List<Integer> listHeight =  new ArrayList<>();                          //List of sheet heights
        int height;                                                             //height of a sheet
        int removedNumbers =0;                                                  //amount of removed sheets
        double relativeDifference;                                              //relative difference between the median and the sheet's height
        for (int i=0; i < sections.size()/2;i++) {                              //for each two successive lines
            height = (parseInt(sections.get(2*i+1).toString()) - parseInt(sections.get(2*i).toString())); //determine the height
            listHeight.add(height);                                             //add the height to the list
        }
        List<Integer> backupListHeight = new ArrayList<>(listHeight);           //Create a new list identical to listHeight
        Collections.sort(backupListHeight);                                     //Sort the new height
        int medianHeight = 0;
        try{
            medianHeight=parseInt(backupListHeight.get(backupListHeight.size()/2).toString()); //get the median from the middle of the sorted list
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        Log.d("height", "checkHeight: "+ medianHeight);
        Log.d("height", "checkHeight: "+ sections);
        Log.d("height", "checkHeight: "+ listHeight);

        for (int i=0; i < listHeight.size();i++){                               //For each sheet
            height = parseInt(listHeight.get(i).toString());                    //get the height
            relativeDifference =(double) (height-medianHeight) / medianHeight;  //calculate the relative difference
            Log.d("Leon", "checkHeight: "+ relativeDifference);
            Log.d("Leon", "checkHeight: "+ height);
            if (relativeDifference <0){                                         //if it's negative
                relativeDifference *= -1;                                       //multiply it by -1
            }
            if (threshold < relativeDifference){                                //if it's superior to the threshold
                sections.remove(2*i+1-2*removedNumbers);                     //remove the top of the sheet from the list
                sections.remove(2*i-2*removedNumbers);                       //remove the bottom of the sheet from the list
                removedNumbers+=1;                                              //the amount of removed sheets increase by 1
            }
        }
        Log.d("height", "checkHeight: "+ sections);
    }



    /**
     * processImage define the order of the processes to find the sheets
     * @param file path to the image
     * @return path to the modified image
     */
    public String processImage(String file) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );         //load the opencv library
        Mat src = Imgcodecs.imread(file);                       //get  the image
        List<Integer> sections =  new ArrayList<>();            //create a list which will contain the position of the sheets

        screeningBlack(sections,src);                           //find the sections
        //screeningColor(sections,src,"black");
        Log.d("Leon", "processImage: " + sections);
        screeningGray(sections,src);                            //remove the sections which aren't gray
        //screeningColor(sections,src,"gray");
        Log.d("Leon", "processImage: " + sections);
        checkHeight(sections);                                  //remove the sections whose height are too different from the median height
        amountSheets = counting (sections);                     //Determine the amount of sheets
        drawCircles(sections,src);                              //Draw circles on the sheets
        Imgcodecs.imwrite(file, src);                           //transform the image into a file
        return file;                                            //return the file
    }

    /**
     * reinitializeImage removes the drawn dots from an image
     * @param reinitialisedImage the image with dots on it
     * @param originalImage the original image without dots
     */
    public void reinitializeImage(String reinitialisedImage,String originalImage){
        Mat src = Imgcodecs.imread(originalImage);                       //get  the image
        Imgcodecs.imwrite(reinitialisedImage, src);                           //transform the image into a file
    }

    /**
     * VariableThreshold finds the threshold where most things were detected and then processes the image for that threshold
     * @param file path to the image
     * @return path to the modified image
     */
    public String VariableThreshold(String file){
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );         //load the opencv library
        Mat src = Imgcodecs.imread(file);                       //get  the image
        List<Integer> sections =  new ArrayList<>();            //create a list which will contain the position of the sheets
        int maxThreshold = 50;                                  //the threshold with the highest amount of detected items
        for (int i=0 ; i < 31 ; i++){                           //for the thresholds of 50 to 200
            threshold = 5*i+50;                                    //determine the new threshold
            screeningBlack(sections,src);                           //find the sections
            Log.d("Leon", "processImage: " + sections);
            screeningGray(sections,src);                            //remove the sections which aren't gray
            Log.d("Leon", "processImage: " + sections);
            checkHeight(sections);                                  //remove the sections whose height are too different from the median height
            if (counting (sections)> amountSheets){                 //if it has more detected items than the previous maximum
                amountSheets = counting (sections);                     //Determine the amount of sheets
                maxThreshold = threshold;                               //assign a new maxThreshold
            }
        }
        threshold = maxThreshold;                               //the threshold is the maxThreshold
        return processImage(file);                              //Process the image with the new threshold
    }

}


