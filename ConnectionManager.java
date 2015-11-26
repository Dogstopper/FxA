import java.util.List;
import java.util.ArrayList;

public class ConnectionManager {

	private List<Connection> connectionList;

	public ConnectionManager() {
		this.connectionList = new ArrayList<>();
	}

	// Return a connection in the connectionList
	public Connection getConnection(RxPPacket packet) {
		// if connection does not exist create a new connection

		short destination = packet.getDestPort();
		short source = packet.getSrcPort();

		// Does this connection already exist?
		for (Connection c : connectionList) {

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {
				return c;
			}
		}

		// Create a new connection
		Connection connection = new Connection(destination, source);
		connectionList.add(connection);
		return connection;
	}

	// Return a connection in the connectionList
	public Connection getConnection(short destination, short source) {
		// if connection does not exist create a new connection

		// Does this connection already exist?
		for (Connection c : connectionList) {

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {
				return c;
			}
		}

		// Create a new connection
		Connection connection = new Connection(destination, source);
		connectionList.add(connection);
		return connection;
	}

	public boolean updateConnection(RxPPacket packet) {
		System.out.println(this.getConnectionPacketStatus(packet));

		boolean connectionUpdated = false; // return this

		Connection c = this.getConnection(packet);

		/* Establishing Connection */ 

		if (!c.isTryingToEstablish()) {
			
			// Does packet contain JUST a SYN?
			if (packet.isSYN() && !packet.isACK() && !packet.isPSH() && !packet.isFIN()) {
				
				c.setTryingToEstablish(true); 
				connectionUpdated = true;
				System.out.println("updated: setTryingToEstablish");
			}
		}

		// Connection could be entering "About To Establish" state
		if (c.isTryingToEstablish() && !c.isAboutToEstablish()) {

			// Does packet contain JUST a SYN - ACK?
			if (packet.isSYN() && packet.isACK() && !packet.isPSH() && !packet.isFIN()) {
				
				c.setAboutToEstablish(true); 
				connectionUpdated = true;
				System.out.println("updated: setAboutToEstablish");
			}
		}

		// Connection could be entering "Establish" state
		if (c.isTryingToEstablish() && c.isAboutToEstablish() && !c.isEstablished()) {

			// Does packet contain JUST a SYN - ACK - PSH?
			if (packet.isSYN() && packet.isACK() && packet.isPSH() && !packet.isFIN()) {
				
				c.setEstablished(true); 
				connectionUpdated = true;
				System.out.println("updated: setEstablished");
			}
		}

		// Connection could be entering "Allowed To Send Data" state
		if (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && !c.isAllowedToSendData()) {

			// Does packet contain JUST a SYN - ACK - PSH - FIN?
			if (packet.isSYN() && packet.isACK() && packet.isPSH() && packet.isFIN()) {
				
				c.setAllowedToSendData(true); 
				connectionUpdated = true;
				System.out.println("updated: setAllowedToSendData");
			}
		}

		// Connection could be entering "Sending Data" state
		if (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && c.isAllowedToSendData() & !c.isSendingData()) {

			// Does packet contain JUST a SYN - ACK - FIN?
			if (packet.isSYN() && packet.isACK() && !packet.isPSH() && packet.isFIN()) {
				
				c.setSendingData(true); 
				connectionUpdated = true;
				System.out.println("updated: setSendingData");
			}
		}

		/* Closing Connection */

		if (!c.isClientSentFIN()) {

			if (packet.isFIN() && !packet.isACK() && !packet.isSYN()) {
				
				c.setClientSentFIN(true);
				connectionUpdated = true;
				System.out.println("updated: setClientSentFIN");
			}
		}

		if (c.isClientSentFIN() && !c.isServerSentACK()) {

			if (!packet.isFIN() && packet.isACK()) {
				
				c.setServerSentACK(true);
				connectionUpdated = true;
				System.out.println("updated: setServerSentACK");
			}
		}

		if (c.isClientSentFIN() && c.isServerSentACK() && !c.isServerSentFIN()) {

			if (packet.isFIN() && !packet.isACK()) {
				
				c.setServerSentFIN(true);
				connectionUpdated = true;
				System.out.println("updated: setServerSentFIN");
			}
		}

		if (c.isClientSentFIN() && c.isServerSentACK() && c.isServerSentFIN() && !c.isClientSentACK()) {

			if (!packet.isFIN() && packet.isACK()) {
				
				c.setClientSentACK(true);
				connectionUpdated = true;
				System.out.println("updated: setClientSentACK");
			}
		}
		
		System.out.println("connectionUpdated: " + connectionUpdated);
		return connectionUpdated;
	} 

	public RxPPacket getNextHandshakePacket(Connection c) {

		short src = c.getSource(),
			  dest = c.getDestination();
		
		int seqNum = 0,
			ackNum = 0;
		
		boolean syn = false, 
				ack = false, 
				psh = false, 
				fin = false;

		// If the initiater of the handshake receives a connection established flag, DO NOT send anymore handshake packets
		// if (c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished())) {
		// 	return null;
		// }

		if (!c.isTryingToEstablish()) {
			
			seqNum = 1;

			syn = true;

			// This is the beginning of the handshake
			c.setIsClient(true);

			System.out.println("TryingToEstablish");
		}

		// Connection could be entering "About To Establish" state
		if (!c.isClient() && (c.isTryingToEstablish() && !c.isAboutToEstablish())) {

			seqNum = 2;

			syn = true;
			ack = true;

			System.out.println("AboutToEstablish");
		}

		// Connection could be entering "Establish" state
		if (c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && !c.isEstablished())) {

			seqNum = 3; 

			syn = true;
			ack = true;
			psh = true;

			System.out.println("Established");
		}

		// Connection could be entering "Allowed To Send Data" state
		if (!c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && !c.isAllowedToSendData())) {

			seqNum = 4;

			syn = true;
			ack = true;
			psh = true;
			fin = true;
			System.out.println("AllowedToSendData");
		}

		// Connection could be entering "Sending Data" state
		if (c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && c.isAllowedToSendData() && !c.isSendingData())) {

			seqNum = 5;

			syn = true;
			ack = true;
			psh = false;
			fin = true;
			System.out.println("SendingData");
		}

		RxPPacket newPacket = new RxPPacket(src,
                                  dest,
                                  seqNum,
                                  ackNum,
                                  fin,
                                  syn,
                                  ack,
                                  psh);

		newPacket.setChecksum(newPacket.calculateChecksum());

		return newPacket;
	}

	public RxPPacket getLastHandshakePacket(Connection c) {

		short src = c.getSource(),
			  dest = c.getDestination();
		
		int seqNum = 0,
			ackNum = 0;
		
		boolean syn = false, 
				ack = false, 
				psh = false, 
				fin = false;


		if (c.isTryingToEstablish() && !c.isAboutToEstablish()) {
			
			seqNum = 1;

			syn = true;

			// This is the beginning of the handshake
			c.setIsClient(true);

			System.out.println("TryingToEstablish");
		}

		// Connection could be entering "About To Establish" state
		if (!c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && !c.isEstablished())) {

			seqNum = 2;

			syn = true;
			ack = true;

			System.out.println("AboutToEstablish");
		}

		// Connection could be entering "Establish" state
		if (c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && !c.isAllowedToSendData())) {

			seqNum = 3; 

			syn = true;
			ack = true;
			psh = true;

			System.out.println("Established");
		}

		// Connection could be entering "Allowed To Send Data" state
		if (!c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && c.isAllowedToSendData())) {

			seqNum = 4;

			syn = true;
			ack = true;
			psh = true;
			fin = true;
			System.out.println("AllowedToSendData");
		}

		// Connection could be entering "Sending Data" state
		if (c.isClient() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && c.isAllowedToSendData() && c.isSendingData())) {

			seqNum = 5;

			syn = true;
			ack = true;
			psh = false;
			fin = true;
			System.out.println("SendingData");
		}

		RxPPacket newPacket = new RxPPacket(src,
                                  dest,
                                  seqNum,
                                  ackNum,
                                  fin,
                                  syn,
                                  ack,
                                  psh);

		newPacket.setChecksum(newPacket.calculateChecksum());

		return newPacket;
	}

	public List<Connection> getConnectionList() {
	    return this.connectionList;
	}

	public RxPPacket getNextClosePacket(Connection c) {

		short src = c.getSource(),
			  dest = c.getDestination();
		
		int seqNum = 0,
			ackNum = 0;
		
		boolean syn = false, 
				ack = false, 
				psh = false, 
				fin = false;

		if (c.isClient() && !c.isClientSentFIN()) {
			
			seqNum = 1;

			fin = true;

			System.out.println("ClientSendingFIN");
		}

		if (!c.isClient() && (c.isClientSentFIN() && !c.isServerSentACK())) {

			// seqNum = 2;
			ackNum = 1;

			ack = true;

			System.out.println("ServerSendingACK");
		}

		if (!c.isClient() && (c.isClientSentFIN() && c.isServerSentACK() && !c.isServerSentFIN())) {

			seqNum = 1; 

			fin = true;

			System.out.println("ServerSendingFin");
		}

		if (c.isClient() && (c.isClientSentFIN() && c.isServerSentACK() && c.isServerSentFIN() && !c.isClientSentACK())) {

			// seqNum = 4;
			ackNum = 1;

			ack = true;

			System.out.println("ClientSendingACK");
		}

		RxPPacket newPacket = new RxPPacket(src,
                                  dest,
                                  seqNum,
                                  ackNum,
                                  fin,
                                  syn,
                                  ack,
                                  psh);

		newPacket.setChecksum(newPacket.calculateChecksum());

		return newPacket;
	}

	public void removeConnection(short destination, short source) {

		for (int i = 0; i < connectionList.size(); i++) {
			Connection c = connectionList.get(i);

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {
				connectionList.remove(i);
				break;
			}
		}
	}

	private String getConnectionPacketStatus(RxPPacket p) {

		Connection c = this.getConnection(p);

		String str = "isClient: " + c.isClient()
				   + "\nisClientSentFIN: " + c.isClientSentFIN()
				   + "\nisServerSentACK: " + c.isServerSentACK()
				   + "\nisServerSentFIN: " + c.isServerSentFIN() 
				   + "\nisClientSentACK: " + c.isClientSentACK()
				   + "\nisFIN: " + p.isFIN() 
				   + "\nisACK: " + p.isACK()
				   + "\nisSYN: " + p.isSYN();

		return str;
	}
}