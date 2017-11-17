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

    private Path drawingPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF660000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Bitmap imageBGBitmap;
    private float brushSize, lastBrushSize;


    private float currentX, currentY;
    private int totalX, totalY;
    private int maxLeft, maxRight, maxTop, maxBottom;
    private float downX, downY;
    private int bitmapWidth, bitmapHeight;
    private int viewWidth, viewHeight;
    private float diffX, diffY;
    private int maxX, maxY;



    private DrawingBuffer dbuff;


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupDrawing();
    }

    public void undo(){
        dbuff.undo(drawCanvas);
    }

    public void redo(){
        dbuff.redo(drawCanvas);
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

        dbuff = new DrawingBuffer(this, drawingPath, drawPaint);
    }

    public Bitmap getCanvasBitmap() {
        return canvasBitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // view given size
        super.onSizeChanged(w, h, oldw, oldh);
        updateNewBmp(w, h);
    }

    public boolean updateNewBmp(int w, int h)
    {
        // loaded an image set it as the background
        imageBGBitmap = null;
        if( selectedImagePath != null ){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds =true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            options.inJustDecodeBounds = false;
            imageBGBitmap = BitmapFactory.decodeFile(selectedImagePath, options);
            Bitmap mutableBitmap = imageBGBitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (imageBGBitmap != null) {
                canvasBitmap = Bitmap.createBitmap(mutableBitmap, 0,0,
                        mutableBitmap.getWidth(),mutableBitmap.getHeight());
            }
        }
        if ( imageBGBitmap == null ){
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        if ( canvasBitmap != null ) {
            drawCanvas = new Canvas(canvasBitmap);
            bitmapWidth = canvasBitmap.getWidth();
            bitmapHeight = canvasBitmap.getHeight();
            viewWidth = getWidth();
            viewHeight = getHeight();
            diffX = bitmapWidth - viewWidth;
            diffY = bitmapHeight - viewHeight;
            maxX = ((bitmapWidth / 2) - (viewWidth / 2));
            maxY = ((bitmapHeight / 2) - (viewHeight / 2));
            maxLeft = (maxX * -1);
            maxRight = maxX;
            maxTop = (maxY * -1);
            maxBottom = maxY;
        }
        else
            Log.e("updateNewBmp", "Cannot create Bitmap!");

        return canvasBitmap != null;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (imageBGBitmap != null) {
            canvas.drawBitmap(canvasBitmap, -diffX/2 + totalX ,
                    -diffY/2 + totalY, canvasPaint);
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
                    //drawingPath.moveTo(touchX, touchY);
                    dbuff.moveTo(MotionEvent.ACTION_DOWN, paintColor, brushSize, touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //drawingPath.lineTo(touchX, touchY);
                    dbuff.lineTo(MotionEvent.ACTION_MOVE, paintColor, brushSize, touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
//                    drawingPath.offset(bitmapWidth/2 - viewWidth/2 - totalX,
//                            bitmapHeight/2 - viewHeight/2  - totalY);
//                    drawCanvas.drawPath(drawingPath, drawPaint);
//                    drawingPath.reset();

                    dbuff.pathDraw(drawCanvas,bitmapWidth/2 - viewWidth/2 - totalX, bitmapHeight/2 - viewHeight/2  - totalY);
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
        brushSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, newSize, getResources()
                        .getDisplayMetrics());
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
        if (isErase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else
            drawPaint.setXfermode(null);
    }

    public void startNew() {
        dbuff.clearBuffers();
        drawCanvas.drawColor(Color.WHITE);
        invalidate();
    }

}