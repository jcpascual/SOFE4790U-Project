package net.uoit.sofe4790.project.client.robot;

public interface IRobotService {
    int SERVICE_ID = 3;

    int METHOD_ID_GET_SCREENSHOT = 1;
    int METHOD_ID_CLICK = 2;
    int METHOD_ID_KEY_PRESS = 3;
    int METHOD_ID_KEY_RELEASE = 4;

    String RETURN_VALUE = "ret";

    byte[] getScreenshot();

    void click(int x, int y, boolean right);

    void keyPress(int keyCode);

    void keyRelease(int keyCode);
}
