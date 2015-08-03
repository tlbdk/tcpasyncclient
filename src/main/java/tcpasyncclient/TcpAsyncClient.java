package tcpasyncclient;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public class TcpAsyncClient {
    SocketAddress socketAddress;

    public TcpAsyncClient(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public CompletableFuture<byte[]> sendReceive(byte[] requestBytes, int maxResponseSize) throws IOException {
        CompletableFuture<byte[]> result = new CompletableFuture<>();

        // Create channel and connect to host
        AsynchronousSocketChannel asynchronousSocketChannel = AsynchronousSocketChannel.open();
        asynchronousSocketChannel.connect(socketAddress, null, new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void v, Void attachment) {
                // Write request
                ByteBuffer writeBuffer = ByteBuffer.wrap(requestBytes);
                asynchronousSocketChannel.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer length, Void attachment) {
                        if (writeBuffer.remaining() > 0) {
                            // More left in the buffer so lets try to write again
                            asynchronousSocketChannel.write(writeBuffer, null, this);

                        } else {
                            // Read response
                            ByteBuffer readBuffer = ByteBuffer.allocate(maxResponseSize);
                            asynchronousSocketChannel.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
                                @Override
                                public void completed(Integer length, Void attachment) {
                                    if(length > 0 && readBuffer.remaining() > 0) {
                                        // More space in buffer and last read gave data
                                        asynchronousSocketChannel.read(readBuffer, null, this);

                                    } else {
                                        byte[] responseBytes = new byte[readBuffer.position()];
                                        readBuffer.position(0);
                                        readBuffer.get(responseBytes);
                                        result.complete(responseBytes);
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, Void attachment) {
                                    result.completeExceptionally(exc);
                                }
                            });
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        result.completeExceptionally(exc);
                    }
                });

            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                result.completeExceptionally(exc);
            }
        });

        return result;
    }

    public TcpAsyncClient(String ip, int port) {
        this(new InetSocketAddress(ip, port));
    }
}
