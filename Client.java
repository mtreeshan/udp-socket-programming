import java.io.IOException;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.lang.Math;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Client {

String serverIP=null;
String Port=null;
DatagramSocket  socket = null;
  boolean flagContinue = false;
    Sender sender = null;
    Receiver receiver = null;

 int seq=-1;
String Version="2";
char SYN='0';
char FIN='0';
char ACK='0';
String post="";
byte[] bytesend =null;
byte[] bytereceive=null;
    DatagramPacket sendP=null;
    DatagramPacket receiveP=null;
int[] flag=new int[12];
   long[] startTime=new long[12];
long[] endTime=new long[12];
int receivenum=0;
int sendnum=0;
long maxRTT=0;
long minRTT=200;
long totalRTT=0;
String bServerTime=null;
String eServerTime=null;

 public class Sender extends Thread {
        Client client = null;
	int seq=0;
	int time=0;
String temp="";
byte[] tempsend=null;
DatagramPacket tempP=null;
        public Sender(Client client,int seq) {
            this.client = client;
	this.seq=seq;
        }
        @Override public void run() {

		SYN='0';
		FIN='0';
		ACK='0';
       		temp=client.makepost(seq,SYN,FIN,ACK);	
		tempsend = temp.getBytes();
    		tempP = new DatagramPacket(tempsend,tempsend.length);	
long starttime=0;
long endtime=0;
		while(flag[seq]!=1&&time<3){
		try{		
			if(time!=0){System.out.println("sequence no:"+seq+",request time out");}
       		 client.socket.send(tempP);
			client.startTime[seq] = System.currentTimeMillis();   
			client.sendnum++;
			Thread.sleep(100);
			time++;
		}catch(Exception e){}
			}


		if(time==3&&flag[seq]!=1){

	System.out.println("sequence no:"+seq+",request time out");
		}else{
			client.receivenum++;
			System.out.println("sequence no:"+seq+", " +client.serverIP+":"+client.Port+",RTT:"+ (endTime[seq] - startTime[seq] ) + "ms");
			totalRTT+=endTime[seq] - startTime[seq];
			if(endTime[seq] - startTime[seq]>client.maxRTT){client.maxRTT=endTime[seq] - startTime[seq];}
			if(endTime[seq] - startTime[seq]<client.minRTT){client.minRTT=endTime[seq] - startTime[seq];}
	
		}
		
        }
    }

public class Receiver extends Thread {
        Client client = null;
	int flagset=1;
	String temp="";
	char first='0';
	char second='0';
	int intfirst=0,intsecond=0;
	int res=0;
	long starttime=0;
	long endtime=0;
        public Receiver(Client client) {
            this.client = client;
        }
        @Override public void run() {
		while(flagContinue){
			try{
        client.bytereceive= new byte[20];
        receiveP = new DatagramPacket(bytereceive, 12);
        socket.receive(receiveP);
       temp=new String(receiveP.getData(), 0, receiveP.getLength());
	
			endtime = System.currentTimeMillis();
			}catch(Exception e){flagset=0;}
			
			if(flagset==1){
			first=temp.charAt(0);
			intfirst=first-'0';
			second=temp.charAt(1);	
			intsecond=second-'0';	
			res=intfirst*10+intsecond;
			flag[res]=1;
			endTime[res]=endtime;
			if(client.bServerTime==null){
				client.bServerTime=temp;
			}
			client.eServerTime=temp;


				}else{
					flagset=1;
				}
						}
	}
}


    public static void main(String[] args) {


	Client client=new Client();
try{

     BufferedReader keyboardInput = null;
 keyboardInput = new BufferedReader(new InputStreamReader(System.in));
	client.serverIP=args[0];
	client.Port=args[1];
	if(!client.connectionStart(args[0],args[1])){
		return;	
	}
	System.out.println("输入发送包的总数:1~96");
	String temp="";
	temp=keyboardInput.readLine();
	int num=Integer.valueOf(temp);
	System.out.println("输入发送包的速率:1~1000(ms)");
	temp=keyboardInput.readLine();
	int speed=Integer.valueOf(temp);
       client.setargs(num);
	for(int i=0;i<num;i++){
		client.sendTo();
		Thread.sleep(speed);
	}
	}catch(Exception e){}
	client.connectionEnd();

    }

    public Client() {
		
    }

public boolean connectionStart(String args0,String args1){
		String ip=args0;
		int port=Integer.valueOf(args1);	
		try{
        	 socket = new DatagramSocket();
socket.connect(new InetSocketAddress(ip, port));
		socket.setSoTimeout(5000);

		String temp="";	
		String post=null;
	seq++;	
		SYN='1';
		FIN='0';
		ACK='0';		
		post=makepost(seq,SYN,FIN,ACK);	
      		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend, bytesend.length);	
       		 socket.send(sendP);

        bytereceive= new byte[20];
        receiveP = new DatagramPacket(bytereceive, 12);
        socket.receive(receiveP);
 
       temp=new String(receiveP.getData(), 0, receiveP.getLength());
					SYN=temp.charAt(3);
					ACK=temp.charAt(5);
			if(SYN=='1'&&ACK=='1'){

seq++;		
		SYN='0';
		FIN='0';	
		ACK='1';	
		post=makepost(seq,SYN,FIN,ACK);		

    		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend, bytesend.length);	
       		 socket.send(sendP);

		System.out.println("成功连接");	

		}else{
			System.out.println("建立连接失败!!!！");
			return false;
		}
		flagContinue=true;
		receiver=new Receiver(this);
		receiver.start();	
				System.out.println( "已经和聊天室服务器建立连接！");
				return true;
		}catch(Exception e){
		System.out.println("建立连接失败！");
		return false;
		}

}

	public void setargs(int total){
		int num=total+15;
		flag=new int[num];
   startTime=new long[num];
endTime=new long[num];
		for(int i=0;i<num;i++){
				flag[i]=0;
  				 startTime[i]=0;
				endTime[i]=0;
	}
}
	public void sendTo(){
		seq++;
		sender=new Sender(this,seq);
		sender.start();
	
}
public void connectionEnd(){
		try{
	flagContinue=false;
Thread.sleep(2500);
System.out.println("接收到包的数量："+receivenum);
System.out.println("丢包率："+(1-(double)receivenum/sendnum));
System.out.println("最大RTT："+maxRTT);
System.out.println("最小RTT："+minRTT);
double aveRTT=(double)totalRTT/receivenum;
System.out.println("平均RTT："+aveRTT);
double total=0;
for(int i=2;i<=seq;i++){
	total+=(endTime[i] - startTime[i]-aveRTT)*(endTime[i] - startTime[i]-aveRTT);
}
System.out.println("RTT的标准差："+Math.sqrt(total));
int hour=(eServerTime.charAt(6)-bServerTime.charAt(6))*10+eServerTime.charAt(7)-bServerTime.charAt(7);
int minute=(eServerTime.charAt(8)-bServerTime.charAt(8))*10+eServerTime.charAt(9)-bServerTime.charAt(9);
int second=(eServerTime.charAt(10)-bServerTime.charAt(10))*10+eServerTime.charAt(11)-bServerTime.charAt(11);
if(second<0){
second+=60;
minute-=1;
}
if(minute<0){
minute+=60;
hour-=1;
}
 	String h="",m="",s="";
		if(hour<10){	h="0";	}
		if(minute<10){	m="0";	}
		if(second<10){	s="0";	}	
		String systemtime=h+hour+":"+m+minute+":"+s+second;

System.out.println("系统整体响应时间:"+systemtime);
Thread.sleep(2500);
String temp="";
seq++;		
		SYN='0';
		FIN='1';	
		ACK='0';	
		post=makepost(seq,SYN,FIN,ACK);	
    		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend, bytesend.length);	
       		 socket.send(sendP);

        bytereceive= new byte[20];
        receiveP = new DatagramPacket(bytereceive, 12);
        socket.receive(receiveP);
       temp=new String(receiveP.getData(), 0, receiveP.getLength());
	

        bytereceive= new byte[20];
        receiveP = new DatagramPacket(bytereceive, 12);
        socket.receive(receiveP);
       temp=new String(receiveP.getData(), 0, receiveP.getLength());	


seq++;		
		SYN='0';
		FIN='0';
		ACK='1';		
		post=makepost(seq,SYN,FIN,ACK);	
	bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend, bytesend.length);	
       		 socket.send(sendP);


		System.out.println("成功释放");	


	
        socket.close();
		
		}catch(Exception e){}
}
	public String makepost(int seq,char SYN,char FIN,char ACK){
		String post="";
		String s="";
		if(seq<10){	s="0";	}
		String meaningless="@";	
		int choice=0;
		for(int i=0;i<14;i++){
			choice=(int)(1+Math.random()*9);
			meaningless+=String.valueOf(choice);
}
		meaningless+="@";
	
		post=s+String.valueOf(seq)+Version+SYN+FIN+ACK+meaningless;
		return post;
}



}

