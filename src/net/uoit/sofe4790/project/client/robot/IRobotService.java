package net.uoit.sofe4790.project.client.robot;

public interface IRobotService {
    int SERVICE_ID = 3;

    int METHOD_ID_GET_SCREENSHOT = 1;
    int METHOD_ID_KEY_PRESS = 3;
    int METHOD_ID_KEY_RELEASE = 4;
    int METHOD_ID_MOUSE_DOWN = 5;
    int METHOD_ID_MOUSE_UP = 6;
    int METHOD_ID_MOUSE_DRAG = 7;

    String RETURN_VALUE = "ret";

    byte[] getScreenshot();

    void keyPress(int keyCode);

    void keyRelease(int keyCode);

    void mouseDown(int x, int y, boolean right);

    void mouseUp(boolean right);

    void mouseDrag(int x, int y);
}
