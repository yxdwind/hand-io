package io.hand.self;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {
    SelectorThread[] threads;
    ServerSocketChannel server = null;
    AtomicInteger xid = new AtomicInteger(0);
    SelectorThreadGroup selectorThreadGroup = this;
    public void setWorkerGroup(SelectorThreadGroup selectorThreadGroup) {
        this.selectorThreadGroup = selectorThreadGroup;
    }

    /**
     * 构造函数，用于初始化SelectorThreadGroup对象
     *
     * @param num 要创建的SelectorThread的数量
     */
    public SelectorThreadGroup(int num) {
        this.threads = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            threads[i] = new SelectorThread(this);
            new Thread(threads[i]).start();
        }
    }

    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            // 注册到哪个selector上？
            nextSelectorV3(server);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 轮询选择一个SelectorThread
     *
     */
    private SelectorThread next() {
        int index = xid.incrementAndGet() % threads.length;
       return threads[index];
    }
    /**
     * 轮询选择一个SelectorThread
     * 可以把序号0默认作为accept线程，其他线程负责R/W
     *
     */
    private SelectorThread nextV2() {
        int index = xid.incrementAndGet() % (threads.length-1);
        return threads[index+1];
    }

    /**
     * 轮询选择一个SelectorThread
     * 可以把序号0默认作为accept线程，其他线程负责R/W
     *
     */
    private SelectorThread nextV3() {
        int index = xid.incrementAndGet() % selectorThreadGroup.threads.length;
        return selectorThreadGroup.threads[index];
    }

    /**
     * 无论serversocket还是socket都要复用这个方法
     * @param channel
     */
    public void nextSelector(Channel channel) {
        SelectorThread thread = next();

        thread.lbq.add(channel);
        thread.selector.wakeup();


//        // thread有可能是server有可能是client
//        ServerSocketChannel server = (ServerSocketChannel) channel;
//        try {
//            // 让selector.select()方法立刻返回不阻塞
//            thread.selector.wakeup();
//            // 会阻塞
//            server.register(thread.selector, SelectionKey.OP_ACCEPT);
//        } catch (ClosedChannelException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * 无论serversocket还是socket都要复用这个方法
     * @param channel
     */
    public void nextSelectorV2(Channel channel) {
        if(channel instanceof ServerSocketChannel){
            threads[0].lbq.add(channel);
            threads[0].selector.wakeup();
        } else if (channel instanceof SocketChannel) {

            SelectorThread thread = nextV2();
            thread.lbq.add(channel);
            thread.selector.wakeup();
        }
    }
    /**
     * 无论serversocket还是socket都要复用这个方法
     * @param channel
     */
    public void nextSelectorV3(Channel channel) {
        if(channel instanceof ServerSocketChannel){
            SelectorThread bossThread = next();
            bossThread.lbq.add(channel);
            bossThread.setWorkerGroup(selectorThreadGroup);
            bossThread.selector.wakeup();
        } else if (channel instanceof SocketChannel) {
            SelectorThread thread = nextV3();
            thread.lbq.add(channel);
            thread.selector.wakeup();
        }
    }
}
