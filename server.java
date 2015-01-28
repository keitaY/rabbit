import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.*;

public class server{
    private final static int NUM = 4;
    private final static String[] QUEUE_NAME = {"TestQueue0","TestQueue1","TestQueue2","TestQueue3"};
    private final static String[] LOGS = {"logs0","logs1","logs2","logs3"};
    public static void main(String[] args) throws IOException, InterruptedException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
	factory.setPassword("26710");
        Connection connection = factory.newConnection();
        Channel[] channel;
	channel = new Channel[NUM];
	
	
	for ( int i = 0; i < NUM; i++ ){
	    channel[i] = connection.createChannel();
	}
	
	
	//    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        
	int people = 0;
	int[] usr;
	usr = new int[NUM];
	int[] ack;
	ack = new int[NUM];
	
	int group=1;
	char[] charray;
	String strbuff;
	QueueingConsumer[] consumer;
	consumer = new QueueingConsumer[NUM];
	
	
	
	for ( int i = 0; i < NUM ; i++){
	    consumer[i] = new QueueingConsumer(channel[i]);
	    channel[i].basicConsume(QUEUE_NAME[i], true, consumer[i]);
	    usr[i] = 0;
	    ack[i] = 0;
	}
	
	
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	
	
	//------------------------------------------------------------------------------------
        
        while(true){
        
	    //------------
        
            for( int i = 0; i < 1; i++){
		QueueingConsumer.Delivery delivery = consumer[i].nextDelivery(10);
		if ( delivery != null){
		    String message = new String(delivery.getBody());
		    if(message.length()>=7 && message.substring(0,5).equals("ENTRY")){
			people++;
			if(people==1){
			    channel[i].basicPublish(LOGS[i], "", null, ("A"+message.substring(5,10)).getBytes());}
			else if(people==2){
			    channel[i].basicPublish(LOGS[i], "", null, ("B"+message.substring(5,10)).getBytes());}
			else if(people==3){
			    channel[i].basicPublish(LOGS[i], "", null, ("C"+message.substring(5,10)).getBytes());}
			else if(people==4){
			    channel[i].basicPublish(LOGS[i], "", null, ("D"+message.substring(5,10)).getBytes());}
			else if(people==5){
			    channel[i].basicPublish(LOGS[i], "", null, ("E"+message.substring(5,10)).getBytes());
			    usr[group] = people;
			    people=0;
			    System.out.printf("　ロビー接続人数　：　%d\n",people);
			    channel[i].basicPublish(LOGS[i], "", null, ("GROUP"+group).getBytes());
			    group++;
			    if(group>=10){group=1;}
			}
			
			System.out.printf("　ロビー接続人数　：　%d\n",people);
		    }

		    else if(message.equals("READY")){
			usr[group] = people;
			people=0;
			System.out.printf("　ロビー接続人数　：　%d\n",people);
			channel[i].basicPublish(LOGS[i], "", null, ("GROUP"+group).getBytes());
			group++;
			if(group>=10){group=1;}
		    }
		    else if(message.equals("EXITEXIT")){
			people = people - 1;
			if(people<0){people=0;}
			System.out.printf("　ロビー接続人数　：　%d\n",people);
		    }
		    System.out.println(" [x] Received '" + message + "' from "+QUEUE_NAME[i]);
		}
	    }
	    
	    //----------------------------------------------
        
        
	    for( int i = 1; i < NUM; i++){
		QueueingConsumer.Delivery delivery = consumer[i].nextDelivery(1);
		if ( delivery != null){
		    String message = new String(delivery.getBody());

		    if(usr[i]!=0&&message.equals("ACKACK")){//端末が部屋につないだらACKを返す。人数分ACKがたまったらSTARTを送る
		      
			ack[i]++;
			System.out.printf("ACK : %d / %d \n",ack[i],usr[i]);
		      
			if(usr[i]!=0 && ack[i]==usr[i]){
			    channel[i].basicPublish(LOGS[i], "", null, ("PEOPLE"+usr[i]).getBytes());
			    channel[i].basicPublish(LOGS[i], "", null, ("START").getBytes());
			    ack[i]=0;}
		    }
		    
		    /*else if(message.equals("EXITEXIT")){//誰かが退場したら中断信号とロビーにもどす信号だす
		
			channel[i].basicPublish(LOGS[i], "", null, ("NOCONTEST").getBytes());
		
		    }*/
		
		    else if(message.length()>6&&message.substring(0,6).equals("_POINT")){ //全ての終了処理が終わったら端末AがFINISHを投げてくる。ロビーに戻す信号だす
      
			ack[i]++;
			System.out.printf("ACK : %d / %d \n",ack[i],usr[i]);
			if(usr[i]!=0 && ack[i]>=usr[i]){
			    ack[i]=0;
			    usr[i]=0;
			}
		    }
		    System.out.println(" [x] Received '" + message + "' from "+QUEUE_NAME[i]);
		
		}
	    }
	}
    }
}
