package uk.co.cothamdigitalservices.chris.splodge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ScrollView;


public class DrawingView extends ScrollView {

    public String selectedImagePath = null;
    public boolean movingImage = false;

    // drawing path
    private Path drawingPath;
    // drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    // initial color
    private int paintColor = 0xFF660000;
    // canvas
    private Canvas drawCanvas;
    // canvas bitmap
    private Bitmap canvasBitmap;
    private Bitmap imageBGBitmap;
    private float brushSize, lastBrushSize;
    private boolean erase = false;

    float downX = 0;
    float downY = 0;
    float currentX, currentY;
    int totalX = 0;
    int totalY = 0;
    int maxLeft = 0;
    int maxRight = 0;
    int maxTop = 0;
    int maxBottom = 0;

    float currScrollX = 0;
    float currScrollY = 0;


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        // get drawing area setup for interaction
        drawingPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
    }

    public Bitmap getCanvasBitmap() {
        return canvasBitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // view given size
        super.onSizeChanged(w, h, oldw, oldh);

        if(!updateNewBmp()) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
        }
    }

    public boolean updateNewBmp()
    {
        // loaded an image set it as the background
        imageBGBitmap = null;
        if(selectedImagePath != null){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds =true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            options.inJustDecodeBounds = false;
            imageBGBitmap = BitmapFactory.decodeFile(selectedImagePath, options);
            Bitmap mutableBitmap = imageBGBitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (imageBGBitmap != null) {
                canvasBitmap = Bitmap.createBitmap(mutableBitmap, 0,0, mutableBitmap.getWidth(),mutableBitmap.getHeight());
                drawCanvas = new Canvas(canvasBitmap);
            }
        }
        return imageBGBitmap != null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // if there is a background then we move the drawing layer with it
        if (imageBGBitmap != null) {
            canvas.drawBitmap(canvasBitmap, maxLeft + totalX, maxTop + totalY, canvasPaint);
        }
        else {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }

        canvas.drawPath(drawingPath, drawPaint);
    }

    private boolean handleMove(MotionEvent event)
    {

        int scrollByX;
        int scrollByY;
        // set maximum scroll amount (based on center of image)
        int maxX = ((canvasBitmap.getWidth() / 2) - (getWidth() / 2));
        int maxY = ((canvasBitmap.getHeight() / 2) - (getHeight() / 2));

        // set scroll limits
        maxLeft = (maxX * -1);
        maxRight = maxX;
        maxTop = (maxY * -1);
        maxBottom = maxY;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();
                scrollByX = (int) -(downX - currentX);
                scrollByY = (int) -(downY - currentY);

                // scrolling to left side of image (pic moving to the right)
                if (currentX < downX) {
                    if (totalX == maxLeft) {
                        scrollByX = 0;
                    }
                    if (totalX > maxLeft) {
                        totalX = totalX + scrollByX;
                    }
                    if (totalX < maxLeft) {
                        scrollByX = maxLeft - (totalX - scrollByX);
                        totalX = maxLeft;
                    }
                }

                // scrolling to right side of image (pic moving to the left)
                if (currentX > downX) {
                    if (totalX == maxRight) {
                        scrollByX = 0;
                    }
                    if (totalX < maxRight) {
                        totalX = totalX + scrollByX;
                    }
                    if (totalX > maxRight) {
                        scrollByX = maxRight - (totalX - scrollByX);
                        totalX = maxRight;
                    }
                }

                // scrolling to top of image (pic moving to the bottom)
                if (currentY < downY) {
                    if (totalY == maxTop) {
                        scrollByY = 0;
                    }
                    if (totalY > maxTop) {
                        totalY = totalY + scrollByY;
                    }
                    if (totalY < maxTop) {
                        scrollByY = maxTop - (totalY - scrollByY);
                        totalY = maxTop;
                    }
                }

                // scrolling to bottom of image (pic moving to the top)
                if (currentY > downY) {
                    if (totalY == maxBottom) {
                        scrollByY = 0;
                    }
                    if (totalY < maxBottom) {
                        totalY = totalY + scrollByY;
                    }
                    if (totalY > maxBottom) {
                        scrollByY = maxBottom - (totalY - scrollByY);
                        totalY = maxBottom;
                    }
                }
                this.scrollBy(scrollByX, scrollByY);
                currScrollX = scrollByX;
                currScrollY = scrollByY;
                downX = currentX;
                downY = currentY;
                break;
        }
        invalidate();
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // detect user touch
        if (movingImage){
            return handleMove(event);
        }
        else {

            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawingPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawingPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    drawingPath.offset(canvasBitmap.getWidth()/2 - getWidth()/2 - totalX, canvasBitmap.getHeight()/2 - getHeight()/2 - totalY);
                    drawCanvas.drawPath(drawingPath, drawPaint);
                    drawingPath.reset();
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        }
    }

    public void setColor(String newColor) {
        // set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newSize) {
        // update size
        float pixelAmount = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, newSize, getResources()
                        .getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public void setErase(boolean isErase) {
        // set erase true or false
        erase = isErase;
        if (erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else
            drawPaint.setXfermode(null);
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

}