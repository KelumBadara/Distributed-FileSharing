import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Node {

	private String ip;
	private int port;
	private String username;

	private DatagramSocket sock;

	private int serverPort;
	private InetAddress serverAddress;

	private List<String> files;
	private ArrayList<Neighbour> neighbourTable;

	public Node(String ip, int port, String username, List<String> files, int serverPort, String sAddress) {

		try {
			sock = new DatagramSocket(port);

			this.ip = ip;
			this.port = port;
			this.username = username;
			this.files = files;
			this.serverPort = serverPort;
			this.serverAddress = InetAddress.getByName(sAddress);
			neighbourTable = new ArrayList<Neighbour>();

			String regResponse = reg();
			echo(regResponse);
			
			String[] regResponseval = regResponse.split(" ");
			String noOfNodes = regResponseval[2];

			if (noOfNodes.equals("0")) {

			} 
			else if (noOfNodes.equals("1")) {
				String neighbourIp = regResponseval[3];
				int neighbourPort = Integer.parseInt(regResponseval[4]);
				
				String joinResponse = this.join(neighbourIp, neighbourPort);
				String[] joinResVal = joinResponse.split(" ");
				if(joinResVal[2].equals("0")){
					Neighbour neighbour = new Neighbour(neighbourIp, neighbourPort, joinResVal[3]);
					neighbourTable.add(neighbour);
				}
				
			} else if (noOfNodes.equals("2")) {
				String neighbourIp = regResponseval[3];
				int neighbourPort = Integer.parseInt(regResponseval[4]);
				
				String joinResponse = this.join(neighbourIp, neighbourPort);
				String[] joinResVal = joinResponse.split(" ");
				if(joinResVal[2].equals("0")){
					Neighbour neighbour = new Neighbour(neighbourIp, neighbourPort, joinResVal[3]);
					neighbourTable.add(neighbour);
				}
				
				neighbourIp = regResponseval[5];
				neighbourPort = Integer.parseInt(regResponseval[6]);
				
				String joinResponse2 = this.join(neighbourIp, neighbourPort);
				String[] joinResVal2 = joinResponse2.split(" ");
				if(joinResVal2[2].equals("0")){
					Neighbour neighbour = new Neighbour(neighbourIp, neighbourPort, joinResVal[3]);
					neighbourTable.add(neighbour);
				}
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
					//echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

					String[] values = s.split(" ");
					String command = values[1];

					switch (command) {

						case "JOIN":
							Neighbour neighbour = new Neighbour(values[2], Integer.parseInt(values[3]), Integer.parseInt(values[4]));
							neighbourTable.add(neighbour);
							int value = 0;// change response by changing this variable
	
							String join_reply = "JOINOK " + value + " " + neighbourTable.size();
	
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
							
						case "LEAVE":
							for(Neighbour neighbour_leave: neighbourTable){
								if(neighbour_leave.getIp().equals(values[2])){
									neighbourTable.remove(neighbour_leave);
								}
							}
							
							int value_leave = 0;// change response by changing this variable
							
							String leave_reply = "LEAVEOK " + value_leave;
	
							int length_leave = leave_reply.length() + 5;
	
							leave_reply = String.format("%04d", length_leave) + " " + leave_reply;
							DatagramPacket leaveReply = new DatagramPacket(leave_reply.getBytes(), leave_reply.getBytes().length,
									incoming.getAddress(), incoming.getPort());
							
							try {
								sock.send(leaveReply);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						
						case "SER" :
							String query = values[4];
							query = query.toLowerCase();
							String[] words = query.split(" ");
							
							ArrayList<String> resultFiles = new ArrayList<String>();
							for(String file: files){
								file = file.toLowerCase();
								String[] words2 = file.split(" ");
								for(String word: words2){
									if(word.equals(words[0])){
										resultFiles.add(file);
									}
								}
							}
							
							if(resultFiles.size() > 0){
								String search_reply = "SEROK " + resultFiles.size() + " " + ip + " " + port + " " + (values[5]+1);
								for(String fileName: files){
									search_reply += (" " + fileName); 
								}
								
								int length_search = search_reply.length() + 5;
		
								search_reply = String.format("%04d", length_search) + " " + search_reply;
								
								try {
									DatagramPacket searchReply = new DatagramPacket(search_reply.getBytes(), search_reply.getBytes().length,
										InetAddress.getByName(values[2]), Integer.parseInt(values[3]));
								
									sock.send(searchReply);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							else{
								for(Neighbour neighbour3: neighbourTable){
									String search_request = "SER " + values[2] + " " + values[3] + " " + values[4] + " " + (values[5] + 1);
									int length_search = search_request.length() + 5;

									search_request = String.format("%04d", length_search) + " " + search_request;

									try {
										DatagramPacket searchRequest = new DatagramPacket(search_request.getBytes(), search_request.getBytes().length,
												InetAddress.getByName(neighbour3.getIp()), neighbour3.getPort());
										sock.send(searchRequest);
									} catch (UnknownHostException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}
							}
							break;
							
						case "SEROK":
							String response = "File found, no of hops = " + values[5] + " ip = " + values[3] + " port = " + values[4];
							
							System.out.println(response);
	
					}

				}

			}
		});
		thread.start();

	}
	
	public String reg(){
		
		String response = null;
		
		try{
			String init_request = "REG " + ip + " " + port + " " + username;
			int length = init_request.length() + 5;
			init_request = String.format("%04d", length) + " " + init_request;
			DatagramPacket regrequest = new DatagramPacket(init_request.getBytes(), init_request.getBytes().length,
					serverAddress, serverPort);
			sock.send(regrequest);
			
			//below code is to receive the response from bootstrap server
			
			byte[] buffer = new byte[65536];
			DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
			
			sock.receive(incoming);
			byte[] data = incoming.getData();
			response = new String(data, 0, incoming.getLength());
			
		}
		catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;		
	}
	
	
	public void unreg() {

		String response = null;
		
		try {
			String unreg_request = "UNREG " + ip + " " + port + " " + username;

			int length = unreg_request.length() + 5;

			unreg_request = String.format("%04d", length) + " " + unreg_request;

			DatagramPacket unregrequest = new DatagramPacket(unreg_request.getBytes(), unreg_request.getBytes().length,
					serverAddress, serverPort);
			sock.send(unregrequest);
			
			
			//below code is to receive the response from bootstrap server
			
			byte[] buffer = new byte[65536];
			DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
			
			sock.receive(incoming);
			byte[] data = incoming.getData();
			response = new String(data, 0, incoming.getLength());

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

	public String join(String neighbourIp, int neighbourPort) {

		String response = null;
		
		try {
			String join_request = "JOIN " + ip + " " + port+ " " + neighbourTable.size();

			int length = join_request.length() + 5;

			join_request = String.format("%04d", length) + " " + join_request;

			DatagramPacket joinRequest = new DatagramPacket(join_request.getBytes(), join_request.getBytes().length,
					InetAddress.getByName(neighbourIp), neighbourPort);
			sock.send(joinRequest);
			
			//below code is to receive the response from bootstrap server
			
			byte[] buffer = new byte[65536];
			DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
			
			sock.receive(incoming);
			byte[] data = incoming.getData();
			response = new String(data, 0, incoming.getLength());


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

		return response;
	}
	
	public void leave() {
		
		try {
			for(Neighbour neighbour: neighbourTable){
				String leave_request = "LEAVE " + ip + " " + port;

				int length = leave_request.length() + 5;

				leave_request = String.format("%04d", length) + " " + leave_request;

				DatagramPacket leaveRequest = new DatagramPacket(leave_request.getBytes(), leave_request.getBytes().length,
						InetAddress.getByName(neighbour.getIp()), neighbour.getPort());
				sock.send(leaveRequest);
				
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

	}
	
	public void search(String query) {
		
		String response = null;
		query = query.toLowerCase();
		String[] words = query.split(" ");
		
		ArrayList<String> resultFiles = new ArrayList<String>();
		for(String file: files){
			file = file.toLowerCase();
			String[] words2 = file.split(" ");
			for(String word: words2){
				if(word.equals(words[0])){
					resultFiles.add(file);
				}
			}
		}
		
		if(resultFiles.size() > 0){
			response = "File found in first node, no of hops = 1";
			for(String fileName: resultFiles){
				response += (" " + fileName);
			}
			System.out.println(response);
		}
		
		else{
			for(Neighbour neighbour3: neighbourTable){
				String search_request = "SER " + ip + " " + port + " " + query + " " + 1;
				int length_search = search_request.length() + 5;

				search_request = String.format("%04d", length_search) + " " + search_request;

				try {
					DatagramPacket searchRequest = new DatagramPacket(search_request.getBytes(), search_request.getBytes().length,
							InetAddress.getByName(neighbour3.getIp()), neighbour3.getPort());
					sock.send(searchRequest);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}

	}
	
	
	public void echoFiles() {
		for(String fileName: files){
			System.out.println(fileName + " ");
		}
	}

	// simple function to echo data to terminal
	public static void echo(String msg) {
		System.out.println(msg);
	}

}
