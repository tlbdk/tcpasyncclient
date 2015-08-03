package tcpasyncclient;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpEchoAsyncServer {
    AsynchronousServerSocketChannel serverChannel;

    public TcpEchoAsyncServer() throws IOException {
        InetSocketAddress hostAddress = new InetSocketAddress(0);
        serverChannel = AsynchronousServerSocketChannel.open().bind(hostAddress);

        // Accept connection
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            public void completed(final AsynchronousSocketChannel channel, Void v) {
                // Read request
                ByteBuffer readBuffer = ByteBuffer.allocate(3000);
                channel.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer length, Void attachment) {
                        // Write response and trim bytes
                        byte[] requestBytes = new byte[readBuffer.position()];
                        readBuffer.position(0);
                        readBuffer.get(requestBytes);
                        ByteBuffer writeBuffer = ByteBuffer.wrap(requestBytes);
                        channel.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {
                            @Override
                            public void completed(Integer result, Void attachment) {
                                try {
                                    channel.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void failed(Throwable exc, Void attachment) {
                                exc.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        exc.printStackTrace();
                    }
                });
            }

            public void failed(Throwable exc, Void v) {
                exc.printStackTrace();
            }
        });
    }

    public InetSocketAddress getLocalAddress() throws IOException {
        return (InetSocketAddress) serverChannel.getLocalAddress();
    }
}
