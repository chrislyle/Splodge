package uk.co.cothamdigitalservices.chris.splodge;

/**
 * Created by chris on 14/11/2017.
 */

class DrawingBufferItem {
    private float brushSize;
    private float xPos;
    private float yPos;

    private int action;
    private int colour;

    DrawingBufferItem(int action, float brushSize, int colour, float xPos, float yPos){
        this.action = action;
        this.brushSize = brushSize;
        this.colour = colour;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    float getBrushSize() {
        return brushSize;
    }
    float getxPos() {
        return xPos;
    }
    float getyPos() {
        return yPos;
    }
    int getAction() {
        return action;
    }
    int getColour() {
        return colour;
    }

}
