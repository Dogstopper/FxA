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

	public Connection newConnection(RxPPacket packet) {
		
		short destination = packet.getDestPort();
		short source = packet.getSrcPort();

		// Does this connection already exist?
		for (Connection c : connectionList) {

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {
				return c;
			}
		}

		Connection connection = new Connection(destination, source);
		
		// Connection should be entering "Trying To Establish" state
		if (!connection.isTryingToEstablish()) {
			
			// Does packet contain JUST a SYN?
			if (packet.isSYN() && !packet.isACK() && !packet.isPSH() && !packet.isFIN()) {
				connection.setTryingToEstablish(true); 
			}
		}
		return connection;
	}

	public Connection updateConnection(RxPPacket packet) {

		short destination = packet.getDestPort();
		short source = packet.getSrcPort();

		// Does this connection already exist?
		for (Connection c : connectionList) {

			if ((c.getDestination() == destination && c.getSource() == source) || (c.getDestination() == source && c.getSource() == destination)) {

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
			}
			return c;
		}
		return null;
	} 

	public List<Connection> getConnectionList() {
	    return this.connectionList;
	}
}