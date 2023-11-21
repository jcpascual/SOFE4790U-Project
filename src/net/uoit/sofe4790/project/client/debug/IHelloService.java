package net.uoit.sofe4790.project.client.debug;

public interface IHelloService {
    int SERVICE_ID = 1;

    int METHOD_ID_GET_HELLO = 1;
    int METHOD_ID_GET_HELLO_WITH_PARAMETER = 2;

    String RETURN_VALUE = "ret";

    String getHello();

    String getHelloWithParameter(String param);
}
