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
            throw new RuntimeException("QueueManager is null: Cannot retrieve queue");
        }
        
        // Validate that startingQueue is not empty or null
        if (startingQueue == null || startingQueue.trim().isEmpty()) {
            LOGGER.error("startingQueue is empty or null");
            throw new RuntimeException("startingQueue cannot be empty or null");
        }
        
        QueueSession queueSession = queueManager.getQueueSession();
        Queue queue = queueSession.getQueue(startingQueue);
        
        //Will find queue with name of startingQueue, if not found will create queue
        LOGGER.info("A queue has been found: " + queue.getName());
        return queue;
    }
    
    public void sendMessage(Queue queue, String sendMessage) {
    	try {
			queue.put(sendMessage);
			LOGGER.info("Message sent to queue: " + queue.getName());
		} catch (InterruptedException e) {
			LOGGER.error("Failed to send message to queue", e);
	        throw new RuntimeException("Failed to send message to queue", e);
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