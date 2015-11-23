import java.util.List;
import java.util.ArrayList;

public class ConnectionManager {

	private static ConnectionManager connectionManager;

	private List<Connection> connectionList;

	public static ConnectionManager get() {

		if (connectionManager == null) {
			connectionManager = new ConnectionManager();
			return connectionManager;
		} else {
			return connectionManager;
		}
	}

	private ConnectionManager() {
		this.connectionList = new ArrayList<>();
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

	public Connection updateConnection(RxPPacket packet) {

		short destination = packet.getDestPort();
		short source = packet.getSrcPort();

		Connection c = this.getConnection(destination, source);

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

			// Does packet contain JUST an ACK - PSH?
			if (!packet.isSYN() && packet.isACK() && packet.isPSH() && !packet.isFIN()) {
				c.setAllowedToSendData(true); 
			}
		}
		
		return c;
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