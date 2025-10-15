package io.hand.self;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 一个线程对应一个selector，多线程情况下，该程序的并发客户端被分配到多个selector上
 * 注意，每个客户端只绑定到其中一个selector
 * 其实并没有交互问题
 */
public class SelectorThread implements Runnable {
    Selector selector = null;
    LinkedBlockingQueue<Channel> lbq = new LinkedBlockingQueue<>();
    SelectorThreadGroup selectorThreadGroup = null;

    public SelectorThread(SelectorThreadGroup selectorThreadGroup) {
        this.selectorThreadGroup = selectorThreadGroup;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SelectorThread(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1 select()
                // 阻塞  select里面有wakeup()
//                System.out.println(Thread.currentThread().getName() + " before select ..." + selector.keys().size());
                int nums = selector.select();
//                System.out.println(Thread.currentThread().getName() + " after select ..." + selector.keys().size());
                // 2处理selectKeys
                if (nums > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    // 线性处理的过程
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        // 最复杂，接受客户端的过程（接受之后，要注册，多线程下新的客户端要注册到哪里？）
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }
                // 3处理一些tasks： listen client
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + "   server listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                        client.register(selector, SelectionKey.OP_READ, byteBuffer);
                        System.out.println(Thread.currentThread().getName() + "   client register read==" + client);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "   readHandler:" + key.attachment());
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        byteBuffer.clear();
        while (true) {
            try {
                int num = client.read(byteBuffer);
                if (num > 0) {
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        client.write(byteBuffer);
                    }
                    byteBuffer.clear();
                } else if (num == 0) {
                    break;
                } else if (num < 0) {
                    // 客户端断开了
                    System.out.println("client:" + client.getRemoteAddress() + " closed...");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "   acceptHandler:" + key.attachment());
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            // choose a selector and register
            selectorThreadGroup.nextSelectorV3(client);

            //            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void setWorkerGroup(SelectorThreadGroup selectorThreadGroup) {
        this.selectorThreadGroup = selectorThreadGroup;
    }
}
