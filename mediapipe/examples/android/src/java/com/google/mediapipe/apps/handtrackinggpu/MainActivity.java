// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.handtrackinggpu;
import android.widget.TextView;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.*;
import android.net.Uri;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.Toast;  
import android.util.Log;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import java.util.HashMap;
import java.lang.*;
import java.util.List;
import android.graphics.Bitmap;
import java.util.Map;
import java.util.*;
import android.util.DisplayMetrics;
import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import android.media.MediaPlayer;
import android.hardware.Camera.PictureCallback;
import  android.hardware.Camera;
import android.view.SurfaceView;
import  java.io.FileOutputStream;
import  android.provider.MediaStore;
import android.graphics.SurfaceTexture;
import  java.text.SimpleDateFormat;

import android.view.MenuItem;



/** Main activity of MediaPipe hand tracking app. */
public class MainActivity extends com.google.mediapipe.apps.basic.MainActivity  {
  private static final String TAG = "MainActivity";
  private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
  private static final String INPUT_MODEL_COMPLEXITY = "model_complexity";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
  // Max number of hands to detect/process.
  private static final int NUM_HANDS = 2;
  int height, width;
  int[] tipIds = {4, 8, 12, 16, 20};
  
  private static final int CAMERA_IMAGE_REQUEST = 101;
  private String imageName;
  int TAKE_PHOTO_CODE = 0;
  int count = 0;



  




  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    
    
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    height = metrics.heightPixels;
    width = metrics.widthPixels;
    
    ApplicationInfo applicationInfo;
    try {
      applicationInfo =
          getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      throw new AssertionError(e);
    }



    AndroidPacketCreator packetCreator = processor.getPacketCreator();
    Map<String, Packet> inputSidePackets = new HashMap<>();
    inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
    if (applicationInfo.metaData.containsKey("modelComplexity")) {
      inputSidePackets.put(
          INPUT_MODEL_COMPLEXITY,
          packetCreator.createInt32(applicationInfo.metaData.getInt("modelComplexity")));
    }
    processor.setInputSidePackets(inputSidePackets);
     

    // To show verbose logging, run:
    // adb shell setprop log.tag.MainActivity VERBOSE
    if (!Log.isLoggable(TAG, Log.VERBOSE)) {
      processor.addPacketCallback(
          OUTPUT_LANDMARKS_STREAM_NAME,
          (packet) -> {
          
            List<NormalizedLandmarkList> multiHandLandmarks =
                PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
                try{

            //Toast.makeText(MainActivity.this, "STRING MESSAGE", Toast.LENGTH_LONG).show();
            //rand1 = multiHandLandmarks.get(0).getLandmarkList().get(4).getY()*1920;
            
            if(Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 18))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 20))  && Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 16))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 14))){

            volumeControl(multiHandLandmarks);
            
            
             
                       }

           if(Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 18))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 20))  && Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 14))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 16))){

            
            brightnessControl(multiHandLandmarks);
          }

          if(Float.parseFloat(getMultiHandLandmarksStringX(multiHandLandmarks, 4))>Float.parseFloat(getMultiHandLandmarksStringX(multiHandLandmarks, 15)) ){

            photoCapture();
          }


            // if((Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 5))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 8))) && (Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 9))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 12))) && 
            //      (Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 13))< Float.parseFloat(getMultiHandLandmarksStringY(multiHandLandmarks, 16)))){
            //   Toast.makeText(MainActivity.this, "First", Toast.LENGTH_LONG).show();
            // }
            
          
          } catch (Exception e){
            Toast.makeText(MainActivity.this, "Error "+e.getMessage(), Toast.LENGTH_LONG).show();
          }
          

            Log.v(
                TAG,
                "[TS:"
                    + packet.getTimestamp()
                    + "] "
                    + getMultiHandLandmarksDebugString(multiHandLandmarks));
          });
    }
  }

  private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }
    String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      multiHandLandmarksStr +=
          "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
        multiHandLandmarksStr +=
            "\t\tLandmark ["
                + landmarkIndex
                + "]: ("
                + landmark.getX()
                + ", "
                + landmark.getY()
                + ", "
                + landmark.getZ()
                + ")\n";
        ++landmarkIndex;
      }
      ++handIndex;
    }

    return multiHandLandmarksStr;
  }


  private void photoCapture(){

    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
            File newdir = new File(dir);
            newdir.mkdirs();

        

                
              count++;
                String file = dir+count+".jpg";
                File newfile = new File(file);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {

                }


                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);


  }




  private String getMultiHandLandmarksStringX(List<NormalizedLandmarkList> multiHandLandmarks, int index) {
    String xcoord = "";
    if (multiHandLandmarks.isEmpty()) {
      return xcoord;
    }
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
          if(landmarkIndex == index){
          // xcoord = landmark.getX(); 
          // y = landmark.getY();
          xcoord = String.format("%.3f", landmark.getX()*width);
        
        // toReturnstring += landmark.getX()
        //         + ", "
        //         + landmark.getY();
              }

        ++landmarkIndex;
      }
      ++handIndex;
    }

    
    return xcoord;
  }

 private String getMultiHandLandmarksStringY(List<NormalizedLandmarkList> multiHandLandmarks, int index) {
    String ycoord = "";
    if (multiHandLandmarks.isEmpty()) {
      return ycoord;
    }
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
          if(landmarkIndex == index){
          // ycoord = landmark.getY(); 
          // y = landmark.getY();
          ycoord = String.format("%.3f", landmark.getY()*height);
        
        // toReturnstring += landmark.getX()
        //         + ", "
        //         + landmark.getY();
              }

        ++landmarkIndex;
      }
      ++handIndex;
    }

    
    return ycoord;
  }


  



  public void volumeControl(List<NormalizedLandmarkList> multiHandLandmarks){
  double minVolSet = 80.0d;
  double maxVolSet = 450.0d;
  double minPhoneVol = 0.0d;
  double maxPhoneVol = 30.0d;
  

  AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE); 
  int volumeLev = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
  int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
  double distance = getDistance(Double.parseDouble(getMultiHandLandmarksStringX(multiHandLandmarks, 4)), Double.parseDouble(getMultiHandLandmarksStringX(multiHandLandmarks, 8)), Double.parseDouble(getMultiHandLandmarksStringY(multiHandLandmarks, 4)), Double.parseDouble(getMultiHandLandmarksStringY(multiHandLandmarks, 8)));  
  double scaledVolLev = scale(distance, minVolSet, maxVolSet, minPhoneVol, maxPhoneVol);          
  audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)Math.round(scaledVolLev), 0);
  String textToInsert =  getMultiHandLandmarksStringX(multiHandLandmarks, 4) + "  " + getMultiHandLandmarksStringY(multiHandLandmarks, 4) + "  " +volumeLev+ "  " + maxVolume + "\n"
  + distance +"  "+ Math.round(scaledVolLev*100.0)/100.0 ;
  
  }

  public void brightnessControl(List<NormalizedLandmarkList> multiHandLandmarks){
  double minVolSet = 80.0d;
  double maxVolSet = 450.0d;
  double minPhoneVol = 0.0d;
  double maxPhoneVol = 255.0d;

  ContentResolver cResolver = getContentResolver();
  
  double distance = getDistance(Double.parseDouble(getMultiHandLandmarksStringX(multiHandLandmarks, 4)), Double.parseDouble(getMultiHandLandmarksStringX(multiHandLandmarks, 8)), Double.parseDouble(getMultiHandLandmarksStringY(multiHandLandmarks, 4)), Double.parseDouble(getMultiHandLandmarksStringY(multiHandLandmarks, 8)));  
  double scaledBrightnessLev = scale(distance, minVolSet, maxVolSet, minPhoneVol, maxPhoneVol);
  Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, (int)Math.round(scaledBrightnessLev));
  }

  public double getDistance(double x1, double x2, double y1, double y2){
    double ac = Math.abs(y2 - y1);
    double cb = Math.abs(x2 - x1);
    double distance =  Math.hypot(ac, cb);

    

        
    return Math.round(distance*100.0)/100.0;
  }


  public  double scale(double valueIn, double baseMin, double baseMax, double limitMin, double  limitMax) {
        double valueToReturn = ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;

        return Math.round(valueToReturn*100.0)/100.0;
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");
        }
    }


    private void CapturePhoto() {

        Log.d("kkkk","Preparing to take photo");
        Camera camera = null;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            int frontCamera = 1;
            //int backCamera=0;

            Camera.getCameraInfo(frontCamera, cameraInfo);

            try {
                camera = Camera.open(frontCamera);
            } catch (RuntimeException e) {
                Log.d("kkkk","Camera not available: " + 1);
                camera = null;
                //e.printStackTrace();
            }
            try {
                if (null == camera) {
                    Log.d("kkkk","Could not get camera instance");
                } else {
                    Log.d("kkkk","Got the camera, creating the dummy surface texture");
                     try {
                         camera.setPreviewTexture(new SurfaceTexture(0));
                        camera.startPreview();
                    } catch (Exception e) {
                        Log.d("kkkk","Could not set the surface preview texture");
                        e.printStackTrace();
                    }
                    camera.takePicture(null, null, new Camera.PictureCallback() {

                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            File pictureFileDir=new File("/sdcard/CaptureByService");

                            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                                pictureFileDir.mkdirs();
                            }
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                            String date = dateFormat.format(new Date());
                            String photoFile = "ServiceClickedPic_" + "_" + date + ".jpg";
                            String filename = pictureFileDir.getPath() + File.separator + photoFile;
                            File mainPicture = new File(filename);

                            try {
                                FileOutputStream fos = new FileOutputStream(mainPicture);
                                fos.write(data);
                                fos.close();
                                Log.d("kkkk","image saved");
                            } catch (Exception error) {
                                Log.d("kkkk","Image could not be saved");
                            }
                            camera.release();
                        }
                    });
                }
            } catch (Exception e) {
                camera.release();
            }
    }


    


    


}

    

   
   

   

     




   



