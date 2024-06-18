import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.lang.Math;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

class Control extends Thread {
    static BufferedReader keyboardInput = null;
	DatagramSocket ss=null;
	Server server=null;
    static {
        keyboardInput = new BufferedReader(new InputStreamReader(System.in));
    }
	public Control(DatagramSocket ss,Server server){
		this.ss=ss;
		this.server=server;
}
    public void run() {
	while(true){
		try{
              		String exit=keyboardInput.readLine();
			if(exit.equals("end")){					
					System.out.println("system endup");
					server.endupflag=true;
					ss.close();
					break;				
			}
		}catch(Exception e){}
			}
    }
}


 class Receiver extends Thread {
	String Version="2";
	int choice=0;
	String post="";
	DatagramSocket socket=null;
	Server server=null;
byte[] bytesend =null;
byte[] bytereceive=null;
    DatagramPacket sendP=null;
    DatagramPacket receiveP=null;
String[] linkip=new String[30];
int[] linkport=new int[30];
int[] beginflag=new int[30];
int[] connectflag=new int[30];
int[] endflag=new int[30];
int clientnum=0;
int pos=0;
int existflag=0;
	public Receiver(DatagramSocket ss,Server server){
		this.socket=ss;
		this.server=server;
        }

	public String makepost(char firstseq,char secondseq,char SYN,char FIN,char ACK){
	String post="";
  	String h="",m="",s="";
	LocalDateTime time=LocalDateTime.now();
		 int hour=time.getHour();
		int minute=time.getMinute();
		int second = time.getSecond();

		if(hour<10){	h="0";	}
		if(minute<10){	m="0";	}
		if(second<10){	s="0";	}	
		String systemtime=h+hour+m+minute+s+second;
		post=String.valueOf(firstseq)+String.valueOf(secondseq)+Version+SYN+FIN+ACK+systemtime;
		return post;
}

        public void run() {
		String  ask=" ";   	
		char firstseq='0',secondseq='0';
		char SYN='0',FIN='0',ACK='0';


		while(!server.endupflag){		//first
	try{

        bytereceive= new byte[30];
        receiveP = new DatagramPacket(bytereceive, 22);
        socket.receive(receiveP);


	InetAddress clientAddress = receiveP.getAddress();
            String clientIP = clientAddress.getHostAddress();
		int clientPort= receiveP.getPort();

	       		 
	       		 
	for(int i=0;i<clientnum;i++){
		if(linkip[i].equals(clientIP)&&linkport[i]==clientPort){
		existflag=1;
		pos=i;
		break;
		}
	}
	if(existflag==0){
		linkip[clientnum]=clientIP;
		linkport[clientnum]=clientPort;
		beginflag[clientnum]=1;
		connectflag[clientnum]=0;
		endflag[clientnum]=0;
		pos=clientnum;
		clientnum++;
	}  
	existflag=0;     		 
       ask=new String(receiveP.getData(), 0, receiveP.getLength());
			firstseq=ask.charAt(0);
			secondseq=ask.charAt(1);	
		SYN=ask.charAt(3);	
		FIN=ask.charAt(4);
		ACK=ask.charAt(5);	

		
		if(SYN=='1'){
		if(beginflag[pos]==0){
	        System.out.println("wrong begin");
	        continue;
		}
	
		SYN='1';
		FIN='0';
		ACK='1';	
		post=makepost(firstseq,secondseq,SYN,FIN,ACK);			//second
		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend,bytesend.length,clientAddress,clientPort);	
       		socket.send(sendP);

		continue;
		}

				

				//third

		if(ACK=='1'&&beginflag[pos]==1){

	        System.out.println(clientIP+":"+clientPort+" success c");	
		beginflag[pos]=0;
		connectflag[pos]=1;	
		continue;	
							}


						//trans
		if(FIN=='1'){		//first

		SYN='0';
		FIN='0';
		ACK='1';	
		post=makepost(firstseq,secondseq,SYN,FIN,ACK);		//second
		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend,bytesend.length,clientAddress,clientPort);	
       		socket.send(sendP);

		SYN='0';
		FIN='1';
		ACK='0';	
		if(secondseq!='9'){
			secondseq+=1;
}else{
			firstseq+=1;
			secondseq='0';	
}
		post=makepost(firstseq,secondseq,SYN,FIN,ACK);			//third
		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend,bytesend.length,clientAddress,clientPort);	
       		socket.send(sendP);
		endflag[pos]=1;
		connectflag[pos]=0;
		continue;
		}

		if(ACK=='1'&&endflag[pos]==1){

	        System.out.println(clientIP+":"+clientPort+" success e");
		endflag[pos]=0;
		beginflag[pos]=1;	
		continue;	
							}


		if(connectflag[pos]==0){
			        System.out.println("wrong communciate");
			        continue;
		}
		choice=(int)(1+Math.random()*11);
		if(choice%5==0){continue;	}


		SYN='0';
		FIN='0';
		ACK='1';	
		post=makepost(firstseq,secondseq,SYN,FIN,ACK);	
		bytesend = post.getBytes();
    		sendP = new DatagramPacket(bytesend,bytesend.length,clientAddress,clientPort);	
       		socket.send(sendP);		
			
    	} catch (Exception e) { }

		}
        }
    }


public class Server {

boolean endupflag=false;
    public static void main(String[] args)  {
			new Server();
    }

public Server(){
try{

 
   		 DatagramSocket socket = new DatagramSocket(12349);

		System.out.println("system setup");
		System.out.println("cin \"end\" if endup system");
		socket.setSoTimeout(5000);	
		endupflag=false;
		Control control=new Control(socket,this);
		control.start();



		Receiver receiver=new Receiver(socket,this);

		receiver.start();

    	} catch (Exception e) { }

}


}
