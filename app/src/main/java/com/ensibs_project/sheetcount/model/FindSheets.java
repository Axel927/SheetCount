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
}
