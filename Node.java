import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.StringTokenizer;

public class Node {

	private String ip;
	private int port;
	private String username;

	DatagramSocket sock;
	DatagramSocket outsock;

	int serverPort;

	InetAddress serverAddress;

	private List<String> files;

	public Node(String ip, int port, String username, List<String> files, int serverPort, String sAddress) {

		try {
			sock = new DatagramSocket(port);

			this.ip = ip;
			this.port = port;
			this.username = username;
			this.files = files;
			this.serverPort = serverPort;

			this.serverAddress = InetAddress.getByName(sAddress);

			// outsock = new DatagramSocket(port);

			String init_request = "REG " + ip + " " + port + " " + username;

			int length = init_request.length() + 5;

			init_request = String.format("%04d", length) + " " + init_request;

			DatagramPacket regrequest = new DatagramPacket(init_request.getBytes(), init_request.getBytes().length,
					serverAddress, serverPort);
			sock.send(regrequest);
			
			
			
			//below code is to receive the response from bootstrap server
			
			byte[] buffer = new byte[65536];
			DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
			try {
				sock.receive(incoming);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] data = incoming.getData();
			String s = new String(data, 0, incoming.getLength());
			
			echo(s);
			
			String[] values = s.split(" ");
			String noOfNodes = values[2];

			if (noOfNodes.equals("0")) {

			} else if (noOfNodes.equals("1")) {
				this.join(values[3], Integer.parseInt(values[4]));

			} else if (noOfNodes.equals("2")) {

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {

					byte[] buffer = new byte[65536];
					DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
					try {
						sock.receive(incoming);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					byte[] data = incoming.getData();
					String s = new String(data, 0, incoming.getLength());

					// echo the details of incoming data - client ip : client
					// port - client message
					echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

					String[] values = s.split(" ");
					String command = values[1];

					switch (command) {

					case "JOIN":

						int value = 0;// change response by changing this
										// variable

						String join_reply = "JOINOK " + value;

						int length = join_reply.length() + 5;

						join_reply = String.format("%04d", length) + " " + join_reply;
						DatagramPacket joinReply = new DatagramPacket(join_reply.getBytes(), join_reply.getBytes().length,
								incoming.getAddress(), incoming.getPort());
						
						
						try {
							sock.send(joinReply);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						break;

					case "JOINOK":
						System.out.println("JOINOK "+ip);
						

					}

				}

			}
		});
		thread.start();

	}

	public void unreg() {

		try {
			String unreg_request = "UNREG " + ip + " " + port + " " + username;

			int length = unreg_request.length() + 5;

			unreg_request = String.format("%04d", length) + " " + unreg_request;

			DatagramPacket unregrequest = new DatagramPacket(unreg_request.getBytes(), unreg_request.getBytes().length,
					serverAddress, serverPort);
			sock.send(unregrequest);
			byte[] receiveData = new byte[1024];

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			sock.receive(receivePacket);
			String responce = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + responce);

			// String[] values = responce.split(" ");
			// String noOfNodes = values[3];

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void join(String neighbourIp, int neighbourPort) {

		try {
			String join_request = "JOIN " + ip + " " + port;

			int length = join_request.length() + 5;

			join_request = String.format("%04d", length) + " " + join_request;

			DatagramPacket unregrequest = new DatagramPacket(join_request.getBytes(), join_request.getBytes().length,
					InetAddress.getByName(neighbourIp), neighbourPort);
			sock.send(unregrequest);
			byte[] receiveData = new byte[1024];

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			sock.receive(receivePacket);
			String responce = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + responce);

			// String[] values = responce.split(" ");
			// String noOfNodes = values[3];

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {

		Node node1 = new Node("localhost", 6500, "Aztec", null, 55555, "localhost");
		
		Node node2 = new Node("localhost", 7500, "Subhash", null, 55555, "localhost");
		
		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		node1.unreg();
	}

	// simple function to echo data to terminal
	public static void echo(String msg) {
		System.out.println(msg);
	}

}
