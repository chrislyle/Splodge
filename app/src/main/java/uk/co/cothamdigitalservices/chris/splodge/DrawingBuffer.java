package uk.co.cothamdigitalservices.chris.splodge;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by chris on 14/11/2017.
 */

class DrawingBuffer {

    private ArrayList<DrawingBufferItem> undoBuffer;
    private ArrayList<DrawingBufferItem> redoBuffer;
    private Paint paint;

    private Path path;
    private DrawingView view;

    DrawingBuffer(DrawingView view, Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
        this.view = view;
        undoBuffer = new ArrayList<>();
        redoBuffer = new ArrayList<>();
    }

    void clearBuffers(){
        undoBuffer.clear();
        redoBuffer.clear();
    }

    void moveTo(int action, int colour, float brushSize, float xpos, float ypos) {
        DrawingBufferItem dbi = new DrawingBufferItem(action, brushSize, colour, xpos, ypos);
        undoBuffer.add(dbi);
        this.paint.setColor(dbi.getColour());
        this.paint.setStrokeWidth(dbi.getBrushSize());
        this.path.moveTo(dbi.getxPos(), dbi.getyPos());
    }

    void lineTo(int action, int colour, float brushSize, float xpos, float ypos) {
        DrawingBufferItem dbi = new DrawingBufferItem(action, brushSize, colour, xpos, ypos);
        undoBuffer.add(dbi);
        this.paint.setColor(dbi.getColour());
        this.paint.setStrokeWidth(dbi.getBrushSize());
        this.path.lineTo(dbi.getxPos(), dbi.getyPos());
    }

    void pathDraw(Canvas canvas, float offsetX, float offsetY) {
        DrawingBufferItem dbi = new DrawingBufferItem(MotionEvent.ACTION_UP,
                0, 0, offsetX, offsetY);
        undoBuffer.add(dbi);
        this.path.offset(offsetX, offsetY);
        canvas.drawPath(this.path, this.paint);
        this.path.reset();
    }

    void undo(Canvas canvas) {
        if (undoBuffer.size() > 0) {
            canvas.drawColor(Color.WHITE);
            this.view.invalidate();

            int last_down = 0;
            // find the first down from the end
            for (int i = undoBuffer.size() - 1; i >= 0; --i) {
                DrawingBufferItem dbi = undoBuffer.get(i);
                redoBuffer.add(0,dbi);
                undoBuffer.remove(i);
                if (dbi.getAction() == MotionEvent.ACTION_DOWN){
                    last_down = i;
                    break;
                }
            }
            for (int i = 0; i < last_down; i++) {
                DrawingBufferItem dbi = undoBuffer.get(i);
                execute(canvas, dbi);
            }
            this.view.invalidate();
        }
    }

    void redo(Canvas canvas) {
        if (redoBuffer.size() > 0) {
            int first_up = 0;
            for (int i = 0; i < redoBuffer.size(); ++i) {
                DrawingBufferItem dbi = redoBuffer.get(i);
                undoBuffer.add(dbi);
                execute(canvas, dbi);
                if (dbi.getAction() == MotionEvent.ACTION_UP){
                    first_up = i;
                    break;
                }
            }
            for (int i = 0; i <= first_up; i++) {
                redoBuffer.remove(0);
            }
            this.view.invalidate();
        }
    }

    private void execute(Canvas canvas, DrawingBufferItem dbi) {
        switch (dbi.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.paint.setColor(dbi.getColour());
                this.paint.setStrokeWidth(dbi.getBrushSize());
                this.path.moveTo(dbi.getxPos(), dbi.getyPos());
                break;
            case MotionEvent.ACTION_MOVE:
                this.paint.setColor(dbi.getColour());
                this.paint.setStrokeWidth(dbi.getBrushSize());
                this.path.lineTo(dbi.getxPos(), dbi.getyPos());
                break;
            case MotionEvent.ACTION_UP:
                this.path.offset(dbi.getxPos(), dbi.getyPos());
                canvas.drawPath(this.path, this.paint);
                this.path.reset();
                break;
            default:
                break;
        }
    }
}
