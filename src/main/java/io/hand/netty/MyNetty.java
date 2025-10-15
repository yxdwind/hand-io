package io.hand.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;

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
}
