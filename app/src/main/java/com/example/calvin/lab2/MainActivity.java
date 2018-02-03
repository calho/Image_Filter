package com.example.calvin.lab2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import static java.lang.Math.*;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;
    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    List<Bitmap> bitmap_history = new ArrayList<>();
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        Button btn = (Button)findViewById(R.id.btn);
        imageView = (ImageView)findViewById(R.id.imageView);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "click!");
                dispatchTakePictureIntent();
            }
        });

        Button gbtn = (Button)findViewById(R.id.gallery);

        gbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "gallery!");
                onPickPhoto(view);
            }
        });

        Button ubtn = (Button)findViewById(R.id.undo);

        ubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "undo!");
                undoTransform(view);
            }
        });

        pref = this.getSharedPreferences("N", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.commit();

        Button sbtn = (Button)findViewById(R.id.save);

        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "save");
                saveImage();
            }
        });

        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);




}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                DialogFragment dialog = new SettingsDialog();
                dialog.show(getSupportFragmentManager(), "settings");
                return true;
            case R.id.action_help:
                DialogFragment helpDialog = new HelpDialog();
                helpDialog.show(getSupportFragmentManager(), "help");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void undoTransform(View view) {

        int N = pref.getInt("N", 5);
        int Q = bitmap_history.size();
        int undo_space = N - Q;
        if (undo_space < 0 && Q != 0) {
            for (int m = 0; m < abs(undo_space); m++) {
                bitmap_history.remove(0);
            }
        }

        int lastIndex = bitmap_history.size() - 1;
        if (lastIndex >= 0){
            Bitmap lastBitmap = bitmap_history.get(lastIndex);
            imageView.setImageBitmap(lastBitmap);
            bitmap_history.remove(lastIndex);
        }

    }


    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Log.d("App","pictureIntent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("PictureIntent", "there's a camera");
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("PictureIntent", "IOException", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("PictureIntent", "pic file created");
                if(Build.VERSION.SDK_INT < 24){
                    Log.d("SDK", String.valueOf(Build.VERSION.SDK_INT));
                    Log.d("PictureIntent", String.valueOf(Uri.fromFile(photoFile)) );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    setResult(RESULT_OK, takePictureIntent);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
                else {
//                    photoFile.getAbsolutePath() = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(this, "com.example.calvin.lab2.fileprovider", photoFile);
                    Log.d("SDK", String.valueOf(Build.VERSION.SDK_INT));
                    Log.d("PictureIntent", String.valueOf(Uri.fromFile(photoFile)) );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    setResult(RESULT_OK, takePictureIntent);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("App","ActivityResult called");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
            try {
                rotateBitmapOrientation(mCurrentPhotoPath);

                Bitmap mImageBitmap;
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("file:" + mCurrentPhotoPath));

                //https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
                ExifInterface exifInterface = new ExifInterface(mCurrentPhotoPath);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                Log.d("App" , String.valueOf(orientation));

//                if (mImageBitmap.getHeight() < mImageBitmap.getWidth()) {
//                    mImageBitmap = rotateImage(mImageBitmap, 90);
//                }
//                switch(orientation) {
//
//                    case ExifInterface.ORIENTATION_ROTATE_90:
//                        mImageBitmap = rotateImage(mImageBitmap, 90);
//                        break;
//
//                    case ExifInterface.ORIENTATION_ROTATE_180:
//                        mImageBitmap = rotateImage(mImageBitmap, 180);
//                        break;
//
//                    case ExifInterface.ORIENTATION_ROTATE_270:
//                        mImageBitmap = rotateImage(mImageBitmap, 270);
//                        break;
//
//                    case ExifInterface.ORIENTATION_NORMAL:
//                    default:
//
//                }
                setPic(mImageBitmap);
//                imageView.setImageBitmap(mImageBitmap);
                //MediaStore.Images.Media.insertImage(getContentResolver(), mImageBitmap, mCurrentPhotoPath, mCurrentPhotoPath);
                galleryAddPic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (data != null && resultCode == RESULT_OK && requestCode == PICK_PHOTO_CODE) {

            try {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                // Load the selected image into a preview
                Log.d("SelectedImage", String.valueOf(photoUri.getPath()));


                String[] projection = { MediaStore.Images.Media.DATA };
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(photoUri, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);
                Log.d("SelectedImage", mCurrentPhotoPath);



//                mCurrentPhotoPath = String.valueOf(photoUri.getPath());
                setPic(selectedImage);
//                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    static String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        try {
            storageDir.mkdirs();
            image.createNewFile();
            Log.d("ImageFile", image.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
//        if(!storageDir.exists()){
//
//            boolean bool = storageDir.mkdirs();
//
//            Log.d("CreateImageFile", "mkdirs()" + String.valueOf(bool));
//        }
//        File image = null;
//        try {
//            Log.d("CreateImageFile", String.valueOf(storageDir.exists()));
//            image = File.createTempFile(
//                    imageFileName,  /* prefix */
//                    ".jpg",         /* suffix */
//                    storageDir      /* directory */
//            );
//
//        }
//        catch(IOException e) {
//            Log.e("CreateImageFile", "IOException", e);
//        }
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("ImageFile" , mCurrentPhotoPath);
        return image;
    }

    private void galleryAddPic() {
        Log.d("galleryAddPic" , "adding image");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Log.d("galleryAddPic", String.valueOf(contentUri));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void saveImage() {
        Log.d("saveImage" , "saving image: " + mCurrentPhotoPath );
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap,mCurrentPhotoPath, mCurrentPhotoPath);

            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            Log.d("saveImage", String.valueOf(contentUri));
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Log.d("rotateImage", "rotating image");
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void setPic(Bitmap mbitmap) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = 480;
        int targetH = 640;

        Log.d("setPic",String.valueOf(targetW) + String.valueOf(targetH));

		/* Get the size of the image */
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
        int photoW = mbitmap.getWidth();
        int photoH = mbitmap.getHeight();

		/* Figure out which way needs to be reduced less */
//        int scaleFactor = 1;
//        if ((targetW > 0) || (targetH > 0)) {
//            scaleFactor = Math.min(photoW/targetW, photoH/targetH);


//        }

        try{
            ExifInterface exifInterface = new ExifInterface(mCurrentPhotoPath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("imageOrientation" , String.valueOf(orientation));
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    mbitmap = rotateImage(mbitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    mbitmap = rotateImage(mbitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    mbitmap = rotateImage(mbitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:

            }

        }catch (IOException e){
            e.printStackTrace();
        }

        Log.d("setPic", String.valueOf(photoH));
        int scaleFactor = 50;
        if ((targetH * targetW) < (photoH*photoW)) {
            mbitmap = Bitmap.createScaledBitmap(mbitmap, targetW, targetH, false);
            imageView.setImageBitmap(mbitmap);
            imageView.setVisibility(View.VISIBLE);
        }
        else{
            imageView.setImageBitmap(mbitmap);
            imageView.setVisibility(View.VISIBLE);
        }



		/* Set bitmap options to scale the image decode target */
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
//        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        //https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a

        /* Associate the Bitmap to the ImageView */

//        imageView.setImageBitmap(compressedBitmap);
//        imageView.setVisibility(View.VISIBLE);

    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Log.d(DEBUG_TAG,"onDoubleTap: " + motionEvent.toString());

//        AsyncTaskSwirl swirl = new AsyncTaskSwirl();
        AsyncTaskTransform transformer = new AsyncTaskTransform();
        transformer.setMode(0);
        Bitmap mImageBitmap;
        mImageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        transformer.execute(mImageBitmap);


        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {


        return false;
    }


    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

//        AsyncTaskBulge bulge = new AsyncTaskBulge();
        AsyncTaskTransform transformer = new AsyncTaskTransform();
        transformer.setMode(1);
        Bitmap mImageBitmap;
        mImageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        transformer.execute(mImageBitmap);
    }

    @Override
    public boolean onFling(MotionEvent m1, MotionEvent m2, float vx, float vy) {

        int SWIPE_THRESHOLD = 100;
        int SWIPE_VELOCITY_THRESHOLD = 100;
        Log.d("onFLing", "fling");

        try{
            float dx = m2.getX() - m1.getX();
            float dy = m2.getY() - m1.getY();

            if (abs(dy) > abs(dx)){
                if (abs(dy) > SWIPE_THRESHOLD && abs(vy) > SWIPE_VELOCITY_THRESHOLD) {
                    if (dy > 0) {
                        Log.d("onFLing", "down swipe");
//                        AsyncTaskInverter inverter = new AsyncTaskInverter();
                        AsyncTaskTransform transformer = new AsyncTaskTransform();
                        transformer.setMode(3);
                        Bitmap mImageBitmap;
                        mImageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        transformer.execute(mImageBitmap);
                    }
                    else {
                        Log.d("onFLing", "up swipe");
//                        AsyncTaskBlur blur = new AsyncTaskBlur();
                        AsyncTaskTransform transformer = new AsyncTaskTransform();
                        transformer.setMode(2);
                        Bitmap mImageBitmap;
                        mImageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        transformer.execute(mImageBitmap);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

//    private class AsyncTaskInverter extends AsyncTask<Bitmap, Void, Bitmap>
//    {
//        Bitmap newBitmap;
//        Bitmap currBitmap;
//
//        @Override
//        protected Bitmap doInBackground(Bitmap... bitmaps) {
//
//            currBitmap = bitmaps[0];
//            newBitmap = currBitmap.copy(currBitmap.getConfig(), true);
//
//            for (int x = 0; x < currBitmap.getWidth(); x++) {
//                for (int y = 0; y < currBitmap.getHeight(); y++) {
//                    int p = 0xFFFFFF - currBitmap.getPixel(x,y);
//                    newBitmap.setPixel(x,y,p );
//                }
//            }
//            return newBitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            imageView.setImageBitmap(bitmap);
//        }
//    }
//
//    private class AsyncTaskBlur extends AsyncTask<Bitmap, Void, Bitmap>
//    {
//        Bitmap newBitmap;
//        Bitmap currBitmap;
//
//        @Override
//        protected Bitmap doInBackground(Bitmap... bitmaps) {
//
//            currBitmap = bitmaps[0];
//            newBitmap = currBitmap.copy(currBitmap.getConfig(), true);
//
//            int width = currBitmap.getWidth();
//            int height = currBitmap.getHeight();
//
//            int[] all_p = new int[(int)width*(int)height];
//            currBitmap.getPixels(all_p, 0, (int)width, 0, 0, (int)width, (int)height );
//
//
//            for (double v = 0; v < currBitmap.getHeight(); v++) {
//
//                for (double u = 0; u < currBitmap.getWidth(); u++) {
//                    //blur
//                    double filterP = 0;
//                    int pixels = 0;
//                    int A = 0;
//                    int R = 0;
//                    int G = 0;
//                    int B = 0;
//                    int blur_r = 5;
//
//                    for (double j = v-blur_r; j < v+blur_r+1; j++) {
//                        for (double i = u-blur_r; i < u+blur_r+1; i++) {
//
//                            if (i>=0 && i < width && j >= 0 && j < height){
//                                int p = all_p[(int)(j*width + i)];
//                                A += alpha(p);
//                                R += red(p);
//                                G += green(p);
//                                B += blue(p);
//
//
//                            }
//
//                            pixels ++;
//
//                        }
//                    }
//                    filterP = argb(A/pixels, R/pixels, G/pixels, B/pixels);
//
//                    newBitmap.setPixel((int)u,(int)v,(int)filterP);
//
//                }
//            }
//            return newBitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            imageView.setImageBitmap(bitmap);
//        }
//    }
//
//
//    private class AsyncTaskBulge extends AsyncTask<Bitmap, Void, Bitmap>
//    {
//        Bitmap newBitmap;
//        Bitmap currBitmap;
//
//        @Override
//        protected Bitmap doInBackground(Bitmap... bitmaps) {
//
//            double factor = 0.005;
//
//            currBitmap = bitmaps[0];
//
//            double width = currBitmap.getWidth();
//            double height = currBitmap.getHeight();
//
//            Log.d("swirl", "w: " + String.valueOf(width) + " h: " + String.valueOf(height));
//
////            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
////            newBitmap = Bitmap.createBitmap(width, height, conf); // this creates a MUTABLE bitmap
//            newBitmap = currBitmap.copy(currBitmap.getConfig(), true);
////            newBitmap.eraseColor(255);
//
//
//            double cX = width/2.0;
//            double cY = height/2.0;
//
//            double error = 0;
//
//            for (double v = 0; v < currBitmap.getHeight(); v++) {
//
//                for (double u = 0; u < currBitmap.getWidth(); u++) {
//
//                    double normU = u/(width-1);
//                    double normV = v/(height-1);
//
//                    //bulge
//
//                    double r = sqrt(pow((normU - 0.5),2) + pow((normV - 0.5),2));
//                    double rn = pow(r,2.5)/0.5;
//                    double a = atan2(normU-.5, normV-.5);
//
//                    double x = (int)((rn*sin(a) + 0.5)*(width-1));
//                    double y = (int)((rn*cos(a) + 0.5)*(height-1));
//
//
//                    if(x >= 0 && x < width && y >= 0 && y < height) {
//                        int p = currBitmap.getPixel((int)x,(int)y);
//
//                        newBitmap.setPixel((int)u,(int)v,p);
//                    }
//
//                }
//            }
//            return newBitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            imageView.setImageBitmap(bitmap);
//        }
//    }

    private class AsyncTaskTransform extends AsyncTask<Bitmap, Void, Bitmap>
    {
        Bitmap newBitmap;
        Bitmap currBitmap;
        int mode;

        public void setMode(int m) {
            mode = m;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {

            double factor = 0.005;

            currBitmap = bitmaps[0];

            double width = currBitmap.getWidth();
            double height = currBitmap.getHeight();

            Log.d("swirl", "w: " + String.valueOf(width) + " h: " + String.valueOf(height));


            newBitmap = currBitmap.copy(currBitmap.getConfig(), true);
            int[] all_p = new int[(int)width*(int)height];
            currBitmap.getPixels(all_p, 0, (int)width, 0, 0, (int)width, (int)height );


            double cX = (width-1)/2.0;
            double cY = (height-1)/2.0;

            double error = 0;

            Log.d("TOUCH", "adding bitmap");
            SharedPreferences.Editor editor = pref.edit();
            int N = pref.getInt("N", 5);
            int Q = bitmap_history.size();
            int undo_space = N - Q;
            if (undo_space <= 0 && Q != 0) {
                for (int m = 0; m < abs(undo_space)+1; m++) {
                    bitmap_history.remove(0);
                }
            }
            if((N - bitmap_history.size()) > 0) {
                bitmap_history.add(((BitmapDrawable)imageView.getDrawable()).getBitmap());
            }

            for (double v = 0; v < currBitmap.getHeight(); v++) {

                for (double u = 0; u < currBitmap.getWidth(); u++) {

                    double normU = u/(width-1);
                    double normV = v/(height-1);

                    double x = 0;
                    double y = 0;

                    double r = 0;

                    switch (mode){
                        case 0:
                            //swirl
                            double dx = u - cX;
                            double dy = v - cY;

                            r = sqrt(pow((normU - 0.5),2) + pow((normV - 0.5),2));

                            double theta = PI * r * 3;

                             x = (int)(floor(((normU - 0.5)*cos(theta)+(normV-0.5)*sin(theta)+0.5)*(width-1)));

                             y = (int)(floor((-(normU - 0.5)*sin(theta)+(normV-0.5)*cos(theta)+0.5)*(height-1)));

                            if(x >= 0 && x < width && y >= 0 && y < height) {
                                int p = currBitmap.getPixel((int)x,(int)y);

                                newBitmap.setPixel((int)u,(int)v,p);
                            }
                            break;
                        case 1:
                            //bulge

                            r = sqrt(pow((normU - 0.5),2) + pow((normV - 0.5),2));
                            double rn = pow(r,2.5)/0.5;
                            double a = atan2(normU-.5, normV-.5);

                            x = (int)((rn*sin(a) + 0.5)*(width-1));
                            y = (int)((rn*cos(a) + 0.5)*(height-1));

                            if(x >= 0 && x < width && y >= 0 && y < height) {
                                int p = currBitmap.getPixel((int)x,(int)y);

                                newBitmap.setPixel((int)u,(int)v,p);
                            }
                            break;
                        case 2:
                            //blur
                            double filterP = 0;
                            int pixels = 0;
                            int A = 0;
                            int R = 0;
                            int G = 0;
                            int B = 0;
                            int blur_r = 5;

                            for (double j = v-blur_r; j < v+blur_r+1; j++) {
                                for (double i = u-blur_r; i < u+blur_r+1; i++) {

                                    if (i>=0 && i < width && j >= 0 && j < height){
                                        int p = all_p[(int)(j*width + i)];
                                        A += alpha(p);
                                        R += red(p);
                                        G += green(p);
                                        B += blue(p);


                                    }

                                    pixels ++;

                                }
                            }
                            filterP = argb(A/pixels, R/pixels, G/pixels, B/pixels);

                            newBitmap.setPixel((int)u,(int)v,(int)filterP);
                            break;
                        case 3:
                            int p = 0xFFFFFF - currBitmap.getPixel((int)u,(int)v);
                            newBitmap.setPixel((int)u,(int)v,p );
                            break;
                    }



//
//                    //rotate
////                    double theta = PI/6.0;
////                    int x = (int)(floor((u - cX)*cos(theta) + (v - cY)*sin(theta) + cX));
////                    int y = (int)(floor(-(u - cX)*sin(theta) + (v - cY)*cos(theta) + cY));
//
//                    //warp
//                    double x = (int)(floor((signum(normU - 0.5)*pow((normU - 0.5),2)/0.5 + 0.5)*(width-1)));
//                    double y = (int)v;
//
//                    //bulge
//
////                    double r = sqrt(pow((normU - 0.5),2) + pow((normV - 0.5),2));
////                    double rn = pow(r,2.5)/0.5;
////                    double a = atan2(normU-.5, normV-.5);
////
////                    double x = (int)((rn*cos(a) + 0.5)*(width-1));
////                    double y = (int)((rn*sin(a) + 0.5)*(height-1));
//
//
//
//



                    //blur
//                    double filterP = 0;
//                    int pixels = 0;
//                    int A = 0;
//                    int R = 0;
//                    int G = 0;
//                    int B = 0;
//                    int blur_r = 5;
//
//                    for (double j = v-blur_r; j < v+blur_r+1; j++) {
//                        for (double i = u-blur_r; i < u+blur_r+1; i++) {
//
//                            if (i>=0 && i < width && j >= 0 && j < height){
//                                int p = all_p[(int)(j*width + i)];
//                                A += alpha(p);
//                                R += red(p);
//                                G += green(p);
//                                B += blue(p);
//
//
//                            }
//
//                            pixels ++;
//
//                        }
//                    }
//                    filterP = argb(A/pixels, R/pixels, G/pixels, B/pixels);
//
//                    newBitmap.setPixel((int)u,(int)v,(int)filterP);

                    //thermal
//                    double filterP = 0;
//                    int pixels = 0;
//                    int A = 0;
//                    int R = 0;
//                    int G = 0;
//                    int B = 0;
//
//                    int p = currBitmap.getPixel((int)u,(int)v);
//                    A = alpha(p);
//                    R = red(p);
//                    G = green(p);
//                    B = blue(p);
//
////                    Integer[] RGB = {R,G,B};
////                    int maxRGB = Collections.max(Arrays.asList(RGB));
////
////                    for (int k = 0; k < RGB.length; k++){
////                        if (RGB[k] != maxRGB){
////                            RGB[k] = (int)(RGB[k]*0.75);
////                        }
////                    }
//
//                    filterP = argb(A, R, G, 0);
//
//                    newBitmap.setPixel((int)u,(int)v,(int)filterP);




                }


            }
            Log.d("transform errors:" , String.valueOf(error));


            Bitmap filteredNewBitmap = newBitmap.copy(newBitmap.getConfig(), true);

            return filteredNewBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
