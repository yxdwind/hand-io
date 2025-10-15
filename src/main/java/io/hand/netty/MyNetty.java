package io.hand.netty;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyNetty {




    @Test
    public void testByteBuf() {
        // 池化，直接内存
//        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(8, 20);

        // 非池化，直接内存
//        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer(8, 20);

        // 非池化，堆内存
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        // 池化，堆内存
//        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);


        print(buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print(buffer);
        buffer.writeBytes(new byte[]{5, 6, 7, 8});
        print(buffer);
        buffer.writeBytes(new byte[]{9, 10, 11, 12});
        print(buffer);
        buffer.writeBytes(new byte[]{13, 14, 15, 16});
        print(buffer);
        buffer.writeBytes(new byte[]{17, 18, 19, 20});
        print(buffer);
//        buffer.writeBytes(new byte[]{21, 22, 23, 24});
//        print(buffer);



    }
    public void print(ByteBuf byteBuf){
        System.out.println("=== ByteBuf 信息 ===");
        System.out.println("ByteBuf: " + byteBuf);
        System.out.println("引用计数: " + byteBuf.refCnt());
        System.out.println("容量: " + byteBuf.capacity());
        System.out.println("最大容量: " + byteBuf.maxCapacity());
        System.out.println("读索引: " + byteBuf.readerIndex());
        System.out.println("写索引: " + byteBuf.writerIndex());
        System.out.println("可读字节数: " + byteBuf.readableBytes());
        System.out.println("可写字节数: " + byteBuf.writableBytes());
        System.out.println("是否可读: " + byteBuf.isReadable());
        System.out.println("是否可写: " + byteBuf.isWritable());
        System.out.println("是否direct: " + byteBuf.isDirect());
        System.out.println("是否连续内存: " + byteBuf.isContiguous());
        System.out.println("=== ByteBuf 信息结束 ===");
    }




    @Test
    public void loopExecutor() throws IOException {
        // group 是一个线程池，默认线程数是 CPU 核心数
        NioEventLoopGroup selector = new NioEventLoopGroup(2);
        selector.execute(() -> {
            try {
                for(;;) {
                    System.out.println("hello world");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        selector.execute(() -> {
            try {
                for(;;) {
                    System.out.println("hello world2");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.in.read();

//        selector.shutdownGracefully();
    }

    @Test
    public void clientMode() throws InterruptedException {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);

        // 客户端模式，每个连接一个线程
        NioSocketChannel client = new NioSocketChannel();

        // epoll_ctl(5,ADD,3)
        thread.register(client);

        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new MyInHandler());


        // 异步的，不会阻塞
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.91.254", 9999));
        ChannelFuture sync = connect.sync();


        ByteBuf byteBuf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture channelFuture = client.writeAndFlush(byteBuf);
        channelFuture.sync();

        sync.channel().closeFuture().sync();

        System.out.println("client over ...");


    }


}

class MyInHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        super.channelRegistered(ctx);
        System.out.println("channelRegistered");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        System.out.println("channelRead");
        ByteBuf buf = (ByteBuf)msg;
        // read会移动指针，读取完之后就没了
//        CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
        CharSequence str = buf.getCharSequence(0,buf.readableBytes(), CharsetUtil.UTF_8);

        System.out.println(str);

        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        super.channelReadComplete(ctx);
        System.out.println("channelReadComplete");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        System.out.println("channelActive");
    }
}
