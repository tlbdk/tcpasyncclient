package tcpasyncclient;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TcpAsyncClientTest {

    @Test
    public void testSendReceive() throws Exception {
        TcpEchoAsyncServer tcpEchoAsyncServer = new TcpEchoAsyncServer(3000);
        InetSocketAddress address = tcpEchoAsyncServer.getLocalAddress();

        final AtomicReference<String> resultString = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        TcpAsyncClient tcpAsyncClient = new TcpAsyncClient("localhost", address.getPort());
        CompletableFuture<byte[]> result = tcpAsyncClient.sendReceive("Hello Server".getBytes(), 3000);
        result.whenComplete((bytes, ex) -> {
            if(ex == null) {
                resultString.set(new String(bytes));

            } else {
                ex.printStackTrace();
            }
            latch.countDown();
        });

        latch.await();
        Assert.assertEquals(resultString.get(), "Hello Server");
    }
}