[comment]: <> (© 2023 by Collen Leon and Tremaudant Axel)

# SheetCount
 
<p align="center">
<img src="https://user-images.githubusercontent.com/77966063/231435291-1ab31966-5637-4741-af38-781bcfc15253.png" alt="drawing" height="500">
</p>

## Table of contents

* [Introduction](#Introduction)
* [Utilisation](#Utilisation)
* [Development](#Development)

<span id="Introduction"><span>
## Introduction

**SheetCount** is an application developed for *Android* to count sheets in an image. The image processing is realised with the library *OpenCV*. The whole development is realised with *Android Studio* in *Java*.

<p align="center">
<img src="https://3.bp.blogspot.com/-yvrV6MUueGg/ToICp0YIDPI/AAAAAAAAADg/SYKg4dWpyC43AAfrDwBTR0VYmYT0QshEgCPcBGAYYCw/s1600/OpenCV_Logo.png" alt="drawing" height="100">
<img src="https://borntocode.fr/wp-content/uploads/2016/05/android-studio-home.png" alt="drawing" height="100">
<img src="https://s3images.coroflot.com/user_files/individual_files/572923_bfaleg9ynacrlnok47k4l8hgp.jpg" alt="drawing" height="100">
</p>



<span id="Utilisation"><span>
## Utilisation

The APK of this software is available in the [release section](https://github.com/Axel927/SheetCount/releases) or is reachable  by scanning the below code QR.

<p align="center">
<img src="https://user-images.githubusercontent.com/77966063/227525296-a2be2ef3-8018-4384-8d62-2c307c214091.png" height="200">
</p>

Once the APK installed and the app started, you should consent the demands of authorisation. Next, you should press the button *Prendre une photo* which will open the camera of the phone. The photo must be taken close to the sheets and the sheets **must be** in the center of the image.  The slider below the photo allow to change a parameter of sheet detection which may upgrade the quality of the count. 

The button *Résumé* give a summary of what has been counted. If you press the button *Exporter les données*, the data will be save in a csv file and you will have the possibility to send the file by email. The content of the email can be settled by clicking the gearwheel.

<span id="Development"><span>
## Development

For using the code, you must install *OpenCV*. To do so, we followed the tutorial from [Medium](https://medium.com/android-news/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c).

NOTA:  
* Step 3 when you select the source location, select *sdk* and not *java*.
* Step 6 after renaming the folder to *jniLibs*, go into the folder and for each software architecture rename the file to *libopencv_java460.so*.


