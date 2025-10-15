package io.hand.self;

/**
 * 这里不做关于IO和业务的事情
 * 1创建 IO thread
 * 2把监听到的server注册到某一个selector
 */
public class MainThread {
    public static void main(String[] args) {
        // 单线程模式
//        SelectorThreadGroup selectorThreadGroup = new SelectorThreadGroup(1);
        // 混合模式，只有一个线程负责accept，每个都会被分配client，进行R/W
//        SelectorThreadGroup selectorThreadGroup = new SelectorThreadGroup(3);


        SelectorThreadGroup boss = new SelectorThreadGroup(3);
        SelectorThreadGroup worker = new SelectorThreadGroup(3);


        boss.setWorkerGroup(worker);
        boss.bind(9999);
        boss.bind(8888);
        boss.bind(7777);
        boss.bind(6666);


    }
}
