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

		Connection c = this.getConnection(packet);

		if (!c.isTryingToEstablish()) {
			
			// Does packet contain JUST a SYN?
			if (packet.isSYN() && !packet.isACK() && !packet.isPSH() && !packet.isFIN()) {
				c.setTryingToEstablish(true); 
			}
		}

		// Connection could be entering "About To Establish" state
		if (c.isTryingToEstablish() && !c.isAboutToEstablish()) {

			// Does packet contain JUST a SYN - ACK?
			if (packet.isSYN() && packet.isACK() && !packet.isPSH() && !packet.isFIN()) {
				c.setAboutToEstablish(true); 
			}
		}

		// Connection could be entering "Establish" state
		if (c.isTryingToEstablish() && c.isAboutToEstablish() && !c.isEstablished()) {

			// Does packet contain JUST a SYN - ACK - PSH?
			if (packet.isSYN() && packet.isACK() && packet.isPSH() && !packet.isFIN()) {
				c.setEstablished(true); 
			}
		}

		// Connection could be entering "Allowed To Send Data" state
		if (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && !c.isAllowedToSendData()) {

			// Does packet contain JUST a SYN - ACK - PSH - FIN?
			if (packet.isSYN() && packet.isACK() && packet.isPSH() && packet.isFIN()) {
				c.setAllowedToSendData(true); 
			}
		}
		
		return packet.isSYN();
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

		if (!c.isTryingToEstablish()) {
			
			syn = true;

			// This is the beginning of the handshake
			c.setSourceInitiatedConnection(true);

			System.out.println("TryingToEstablish");
		}

		// Connection could be entering "About To Establish" state
		if (!c.sourceInitiatedConnection() && (c.isTryingToEstablish() && !c.isAboutToEstablish())) {

			syn = true;
			ack = true;

			System.out.println("AboutToEstablish");
		}

		// Connection could be entering "Establish" state
		if (c.sourceInitiatedConnection() && (c.isTryingToEstablish() && c.isAboutToEstablish() && !c.isEstablished())) {

			syn = true;
			ack = true;
			psh = true;

			System.out.println("Established");
		}

		// Connection could be entering "Allowed To Send Data" state
		if (!c.sourceInitiatedConnection() && (c.isTryingToEstablish() && c.isAboutToEstablish() && c.isEstablished() && !c.isAllowedToSendData())) {

			syn = true;
			ack = true;
			psh = true;
			fin = true;
			System.out.println("AllowedToSendData");
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

	public void removeConnection(short destination, short source) {

		for (int i = 0; i < connectionList.size(); i++) {
			Connection c = connectionList.get(i);

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {
				connectionList.remove(i);
				break;
			}
		}
	}
}