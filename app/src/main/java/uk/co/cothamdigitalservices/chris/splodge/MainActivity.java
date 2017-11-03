package uk.co.cothamdigitalservices.chris.splodge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.UUID;

import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 78123;
    private static final int SELECT_PICTURE = 1;
    private DrawingView drawView;
    private ImageButton currPaint;
    private ImageButton toggleMoveBtn;
    private float smallBrush, mediumBrush, largeBrush;

    Bitmap bmp_on;
    Bitmap bmp_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        drawView = findViewById(R.id.drawing);
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        ImageButton openGalleryBtn = findViewById(R.id.open_gallery_btn);
        openGalleryBtn.setOnClickListener(this);
        toggleMoveBtn = findViewById(R.id.toggle_move_btn);
        toggleMoveBtn.setOnClickListener(this);
        ImageButton drawBtn = findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);
        drawView.setBrushSize(mediumBrush);
        ImageButton eraseBtn = findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        ImageButton newBtn = findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);
        ImageButton saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        // toolbar stuff
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.toggle_move_on_btn, options);
        options.inJustDecodeBounds = false;
        bmp_on = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_move_on_btn, options);
        bmp_off = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_move_off_btn, options);
    }

    public void paintClicked(View view) {
        // use chosen color
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        if (view != currPaint) {
            // update color
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));
            currPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                drawView.selectedImagePath = getPath(selectedImageUri);
                drawView.updateNewBmp();
            }
        }
    }


    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        String path = null;
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(column_index);
            }
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    @Override
    public void onClick(View view) {
        // respond to clicks
        if (view.getId() == R.id.draw_btn) {
            // draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = brushDialog
                    .findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = brushDialog
                    .findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = brushDialog
                    .findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        } else if (view.getId() == R.id.erase_btn) {
            // switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = brushDialog
                    .findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);

                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = brushDialog
                    .findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = brushDialog
                    .findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        } else if (view.getId() == R.id.new_btn) {
            // new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog
                    .setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            drawView.startNew();
                            dialog.dismiss();
                        }
                    });
            newDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            newDialog.show();

        } else if (view.getId() == R.id.open_gallery_btn) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), SELECT_PICTURE);

        } else if (view.getId() == R.id.toggle_move_btn) {
            drawView.movingImage = !drawView.movingImage;
            if (drawView.movingImage)
                toggleMoveBtn.setImageBitmap(bmp_on);
            else
                toggleMoveBtn.setImageBitmap(bmp_off);

        } else if (view.getId() == R.id.save_btn) {
            // save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // save drawing
                            drawView.setDrawingCacheEnabled(true);
                            String imgSaved = MediaStore.Images.Media
                                    .insertImage(getContentResolver(), drawView.getCanvasBitmap(), UUID
                                                    .randomUUID().toString() + ".png",
                                            "drawing");
                            if (imgSaved != null) {
                                Toast savedToast = Toast.makeText(
                                        getApplicationContext(),
                                        "Drawing saved to Gallery!",
                                        Toast.LENGTH_SHORT);
                                savedToast.show();
                            } else {
                                Toast unsavedToast = Toast.makeText(
                                        getApplicationContext(),
                                        "Oops! Image could not be saved.",
                                        Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        }
                    });
            saveDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            saveDialog.show();
        }
    }
}