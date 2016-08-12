/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-09 10:25 创建
 */

import com.yiji.falcon.agent.util.BlockingQueueUtil;
import com.yiji.falcon.agent.util.ExecuteThreadUtil;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
public class JMXConnectWithTimeout {

    public static JMXConnector connectWithTimeout( final JMXServiceURL url, long timeout, TimeUnit unit) throws IOException {
        final BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);
        ExecuteThreadUtil.execute(() -> {
            try {
                JMXConnector connector = JMXConnectorFactory.connect(url);
                if (!blockingQueue.offer(connector))
                    connector.close();
            } catch (Throwable t) {
                blockingQueue.offer(t);
            }
        });

        Object result = BlockingQueueUtil.getResult(blockingQueue,timeout,unit);

        if (result == null)
            throw new SocketTimeoutException("Connect timed out: " + url);
        if (result instanceof JMXConnector)
            return (JMXConnector) result;
        try {
            throw (Throwable) result;
        } catch (IOException | RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            // In principle this can't happen but we wrap it anyway
            throw new IOException(e.toString(), e);
        }
    }

}