package com.mule.runtime.muleServerNotifications.demo;

import javax.inject.Inject;

import org.mule.runtime.core.api.context.notification.MuleContextNotification;
import org.mule.runtime.core.api.context.notification.MuleContextNotificationListener;
import org.mule.runtime.core.api.util.queue.Queue;
import org.mule.runtime.core.api.util.queue.QueueManager;
import org.mule.runtime.core.api.util.queue.QueueSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MuleContextListener implements MuleContextNotificationListener<MuleContextNotification> {
	
    private static Logger LOGGER = LoggerFactory.getLogger(MuleContextListener.class);
    private String startingQueue;
    private String sendMessage;
    
    @Inject
    private QueueManager queueManager;
    
    
    public void setStartingQueue(String startingQueue)
    {
        this.startingQueue = startingQueue;
    }
    
    public void setSendMessage(String sendMessage)
    {
        this.sendMessage = sendMessage;
    }
    
    public Queue getVmQueue(String startingQueue, QueueManager queueManager) {
        if (queueManager == null) {
            LOGGER.error("queueManager is null!");
            return null;
        }
        QueueSession queueSession = queueManager.getQueueSession();
        Queue queue = queueSession.getQueue(startingQueue);
        LOGGER.info("Queue Found");
        
        return queue;
    }
    
    public void sendMessage(Queue queue, String sendMessage) {
    	try {
			queue.put(sendMessage);
			LOGGER.info("Message sent to queue");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onNotification(MuleContextNotification muleContext) {		
		LOGGER.info("Mule Context Action Name is: " + muleContext.getActionName());
		if (muleContext.getAction().getActionId() == MuleContextNotification.CONTEXT_STARTED) {
			LOGGER.info("Mule context started");
			Queue queue = getVmQueue(startingQueue, queueManager);
			sendMessage(queue, sendMessage);
		}
	}
}