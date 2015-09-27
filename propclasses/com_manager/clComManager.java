//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clComManager.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Im Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com
//

package org.androidtown.sleeper.propclasses.com_manager;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clComManager.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook
//  @ Email : rkdtlsdnr102@naver.com


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.androidtown.sleeper.endclasses.clFanMessageConverter;
import org.androidtown.sleeper.endclasses.clTempSensorMessageConverter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that gives app interface to communicate with ap device.
 * This class uses transport-layer to communicate. Since device is in ap mode, request message is
 * always sent from app and response message is always made from controlling side. In order to make sure
 * that communication is processed independently between each objects that holds instance of this class,
 * ComManager holds ip address and port number as static variable but holds socket as local variable so
 * each can create individual client socket.
 *
 * You have to know port number of device and we recommend to unify all port number of device to
 * 5000 so that it doesn't need to change port number every time it connects to different devices.
 */
public class clComManager{

	private IMessageListener MessageListener=null;

	//port number is always 5000, also on device
	private static int port=5000 ;
	private static InetAddress ipAddr=null ;
	private static int timeout_unit =10000 ;
	private static int timeout_count =2 ;

    private byte endChar=126 ;

	private DataInputStream diStream = null ;
	private DataOutputStream doStream = null ;

	//request message queue in order to send message fifo
	private Queue<clRequestMessage> reqMsgQueue =null ;
    //list of communication task
    private List<clComManagerThreadTask> taskList =null ;
    //additional queue for request needs response message or not
    private Queue<Boolean> needRespMsgQueue=null ;

    /**
     * Constructor
     * @param messageListener message listener
     */
	public clComManager(IMessageListener messageListener){

        MessageListener=messageListener ;
		reqMsgQueue =new ConcurrentLinkedQueue<>() ;
        taskList =new CopyOnWriteArrayList<>() ;
        needRespMsgQueue=new ConcurrentLinkedQueue<>() ;
    }

    /**
     * Constructor
     */
    public clComManager(){

        reqMsgQueue =new ConcurrentLinkedQueue<>() ;
        taskList =new CopyOnWriteArrayList<>() ;
        needRespMsgQueue=new ConcurrentLinkedQueue<>() ;
    }

    /**
     * Set message listener
     * @param _MessageListener message listener for ComManager
     */
	public void setMessageListener(IMessageListener _MessageListener){

		MessageListener=_MessageListener ;
	}

    /**
     * Get message listener instance holds
     * @return message listener instance holds
     */
	public IMessageListener getMessageListener(){

		return MessageListener ;
	}

	/**
	 * Set ip address of device this app connects to
	 * @param _ipAddr ip address of device
	 */
	public static void setIpAddr(InetAddress _ipAddr){

		ipAddr=_ipAddr ;
	}


    /**
     * Set port
     * @param portNum port number
     */
	public static void setPort(int portNum){

		port=portNum ;
	}

    /**
     * Set timeout
     * @param timeoutMillis timeout in millisecond
     */
    public void setTimeoutUnit(int timeoutMillis){

        timeout_unit =timeoutMillis ;

    }

    /**
     * Set timeout count
     * @param timeoutCnt timeout count
     */
    public void setTimeoutCount(int timeoutCnt){

        timeout_count =timeoutCnt ;
    }


	/**
	 * Connect to ap device
	 */
    public void connect(){

        clComManagerThreadTask task=new clComManagerThreadTask() ;

        //add task to list
        taskList.add(task) ;

        new Thread(task).start();
    }

	/**
	 * Disconnect from ap device. Since it disconnects automatically after receiving response message,
     * user usually doesn't have to call it, but sometimes, not frequent, it would wait for response message
     * even measure is stopped since it message transfer is done in different thread. So user is recommended to
     * call this function when measure is stopped in user's processor.
	 */
	public void disconnect(){

        //do not erase
        synchronized (taskList) {

            for(int i=0;i<taskList.size();i++)
                taskList.remove(i).disconnect();

        }

	}
    /**
     * Send request message to device
     * it adds request message to queue and thread will poll request message fifo.
     * it has to be synchronized since it is used in both main thread, worker thread
     * @param reqMsg request message to send
     * @param needResponse true if request needs response message, otherwise false
     */
    public synchronized void send(clRequestMessage reqMsg,boolean needResponse){

        reqMsgQueue.add(reqMsg) ;
        needRespMsgQueue.add(needResponse) ;

    }

    private class clComManagerThreadTask implements Runnable {

        private Socket devSocket=null ;


        //code for testing message translating, do not erase
        @Override
        public void run() {

            clRequestMessage reqMsg = null;

            //if there're some request message to send

            while (reqMsgQueue.isEmpty()) ;

            //if (!reqMsgQueue.isEmpty()) {

            reqMsg = reqMsgQueue.poll();

            byte[] reqMessageString=reqMsg.makeMessage().getBytes() ;
            Log.i(toString(), "Req: " + reqMsg.makeMessage());
            Log.i(toString(), "Req ControlInfo: " + reqMsg.getDeviceMessage());
            Log.i(toString(), "Message Sent");

            Log.i("end of byte",Byte.toString(reqMessageString[reqMessageString.length-1])) ;


            //wait for 500 millisecond for receiving

            if (needRespMsgQueue.poll()) {

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                    Log.d(e.toString(), e.getMessage());
                }

                String rcvStream = "";

                if (reqMsg.getDeviceID() == clTempSensorMessageConverter.TEMP_ID) {

                    rcvStream = (char) clResponseMessage.RES + "\n" + (char) clResponseMessage.SUCCESS + "\n" +
                            (char) clTempSensorMessageConverter.TEMP_ID + "\n" + (char) clTempSensorMessageConverter.MEASURE_TEMPERATURE +
                            "" + (char) 35 + "\n";

                } else if (reqMsg.getDeviceID() == clFanMessageConverter.FAN_ID) {

                    clFanMessageConverter fanMessageConverter = new clFanMessageConverter();

                    fanMessageConverter.dissolveDeviceMessage(reqMsg.getDeviceMessage());

                    if (fanMessageConverter.getCommand() == clFanMessageConverter.PWM_SET) {

                        rcvStream = (char) clResponseMessage.RES + "\n" + (char) clResponseMessage.SUCCESS + "\n" +
                                (char) clFanMessageConverter.FAN_ID + "\n" + (char) clFanMessageConverter.PWM_SET +
                                "" + (char) fanMessageConverter.getData() + "\n";
                    }

                }

                Log.i(toString(), "Received Message: " + rcvStream);

                clResponseMessage resMsg = new clResponseMessage();

                resMsg.dissolveMessage(rcvStream);

                taskList.remove(this);

                MessageListener.onReceiveMessageEvent(resMsg);

            } else {

                taskList.remove(this);

            }

        }



//code for real, do not erase
/*
        @Override
        public void run() {

            //if ip address is set
            if (ipAddr != null) {

                try {
                    //initialize socket
                    devSocket = new Socket(ipAddr, port);
                    devSocket.setSoTimeout(timeout_unit);

                    //use input stream, output stream of created socket
                    try {
                        diStream = new DataInputStream(devSocket.getInputStream());
                        doStream = new DataOutputStream(devSocket.getOutputStream());

                    } catch (IOException e) {

                        Log.e(toString(), e.getMessage());
                        Log.e(toString(), "Error creating input, output stream");
                    }

                    //while devSocket is alive and not closed
                    while (reqMsgQueue.isEmpty()) ;

                    //if there're some request message to send
                    clRequestMessage reqMsg = null;
                    String rcvStream = "";

                    //poll one request message from queue
                    reqMsg = reqMsgQueue.poll();

                    try {
                        byte[] bytes = reqMsg.makeMessage().getBytes();

                        //doStream.writeChars(reqMsg.makeMessage());
                        doStream.write(reqMsg.makeMessage().getBytes());

                        //if request message needs response message
                        if(needRespMsgQueue.poll()) {

                            byte ch;
                            int timeoutCnt = 0;
                            boolean responseReceived = false;
                            //if timeout count has exceeded maximum count or
                            //response message is received

                            while (timeoutCnt != timeout_count) {

                                try {

                                    ch = diStream.readByte();//read one byte each time

                                    if (ch == endChar) {
                                        //set response receive flag true for indicating successful receiving response message
                                        responseReceived = true;
                                        //disconnect after message is received
                                        break;
                                    } else {

                                        //if it reached end of one string
                                        rcvStream += (char) ch;
                                    }

                                } catch (Exception e) {

                                    Log.e("Any exception", "Unknown exception occured");
                                    timeoutCnt++;
                                }
                            }

                            //if got response message successfully
                            disconnect();//disconnect socket

                            try {
                                Thread.sleep(1000);

                            } catch (InterruptedException e) {

                            }


                            if (responseReceived) {

                                final clResponseMessage resMsg = new clResponseMessage();

                                resMsg.dissolveMessage(rcvStream);

                                //create handler and post it on main looper for synchronizing with
                                //main looper
                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {

                                        MessageListener.onReceiveMessageEvent(resMsg);
                                    }

                                });

                            }
                        }else
                        {
                            disconnect() ;
                        }


                    } catch (IOException e) {

                        Log.e("Disconnection Occured", e.getMessage());
                        disconnect();
                    }

                } catch (IOException e) {//if error occured with creating socket

                    Log.e("Error Creating Socket", e.getMessage());
                    //startFlag=false ;
                    devSocket=null ;
                }

            } else {//if ip address is not set
                Log.e(toString(), "No Ip Address Set");
            }
        }
*/

        private void disconnect(){

            //do not erase
            try {

                if (devSocket != null) {

                    devSocket.close();
                    devSocket = null;

                    Log.i(toString(), "Disconnecting and closing socket");
                }

            } catch (IOException e) {

                Log.e(toString(), e.getMessage());
                Log.e(toString(), "Disconnecting and closing socket failed");

            } finally {

                devSocket = null;

                //remove this task from task thread on disconnection
                taskList.remove(this) ;
            }

        }
    }

    /**
     * Interface for message event.
     */
    public interface IMessageListener {
        /**
         * Triggered when response message is received
         * @param ResponseMessage response message received
         */
        void onReceiveMessageEvent(clResponseMessage ResponseMessage);
    }
}
