package net.uoit.sofe4790.project.client.robot;

import net.uoit.sofe4790.project.client.rpc.RpcLocalService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RobotLocalService extends RpcLocalService implements IRobotService {
    private final Robot robot;

    public RobotLocalService() {
        // Create a Robot instance for controlling this computer.
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public byte[] getScreenshot() {
        // Get the bounds of the default screen.
        Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        // Create a screen capture.
        BufferedImage image = robot.createScreenCapture(rectangle);

        // Create a ByteArrayOutputStream to hold the serialized image.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Serialize it to PNG format.
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stream.toByteArray();
    }

    @Override
    public void click(int x, int y, boolean right) {
        // Move the mouse to these coordinates.
        robot.mouseMove(x, y);

        // Choose the right mask.
        int mask = right ? InputEvent.BUTTON2_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK;

        // Press the mouse button and release it.
        robot.mousePress(mask);
        robot.mouseRelease(mask);
    }

    @Override
    public void keyPress(int keyCode) {
        System.out.println("key down");
        // Press the key.
        robot.keyPress(keyCode);
        System.out.println("key down done");
    }

    @Override
    public void keyRelease(int keyCode) {
        System.out.println("key up");
        // Release the key.
        robot.keyRelease(keyCode);
        System.out.println("key up done");
    }

    @Override
    public RpcResult handleRequest(int methodId, RpcBundle bundle) {
        // Create an empty RpcResult instance.
        RpcResult result = new RpcResult();
        result.success = true;

        if (methodId == METHOD_ID_GET_SCREENSHOT) {
            // Take a screenshot and put the result into the result bundle.
            result.bundle.putByteArray(RETURN_VALUE, getScreenshot());
        } else if (methodId == METHOD_ID_CLICK) {
            // Get the parameters from the input bundle.
            int x = bundle.getInt("x");
            int y = bundle.getInt("y");
            boolean right = bundle.getBoolean("right");

            // Click the mouse.
            click(x, y, right);
        } else if (methodId == METHOD_ID_KEY_PRESS) {
            int keyCode = bundle.getInt("keyCode");

            keyPress(keyCode);
        } else if (methodId == METHOD_ID_KEY_RELEASE) {
            int keyCode = bundle.getInt("keyCode");

            keyRelease(keyCode);
        } else {
            throw new RuntimeException("Invalid method ID");
        }

        return result;
    }
}
