package net.uoit.sofe4790.project.common.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LoginResponseFailMessage extends Message {
    @Override
    protected int getMessageType() {
        return Message.TYPE_LOGIN_RESPONSE_FAIL;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {

    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {

    }
}
