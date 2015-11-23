public class Connection {

	private short destination;
	private short source;

	private boolean sourceInitiatedConnection; // True if Host Initiated Connection

	private boolean isTryingToEstablish;
	private boolean isAboutToEstablish;
	private boolean isEstablished;
	private boolean isAllowedToSendData;

	public Connection(short destination, short source) {

		this.destination = destination;
		this.source = source;

		this.sourceInitiatedConnection = false;

		this.isTryingToEstablish = false;
		this.isAboutToEstablish = false;
		this.isEstablished = false;
		this.isAllowedToSendData = false;
	}

	public String connectionStateToString() {

		String state = this.source + "  -->  " + this.destination
					+ "\nisTryingToEstablish: " + (this.isTryingToEstablish ? "1" : "0")
					+ "\nisAboutToEstablish: " + (this.isAboutToEstablish ? "1" : "0")
					+ "\nisEstablished: " + (this.isEstablished ? "1" : "0")
					+ "\nisAllowedToSendData: " + (this.isAllowedToSendData ? "1" : "0");

		return state;
	}

	/* Setters and Getters */

	public boolean sourceInitiatedConnection() {
	    return this.sourceInitiatedConnection;
	}

	public short getDestination() {
	    return this.destination;
	}

	public short getSource() {
	    return this.source;
	}

	public boolean isTryingToEstablish() {
	    return this.isTryingToEstablish;
	}

	public boolean isAboutToEstablish() {
	    return this.isAboutToEstablish;
	}

	public boolean isEstablished() {
	    return this.isEstablished;
	}

	public boolean isAllowedToSendData() {
	    return this.isAllowedToSendData;
	}

	public void setSourceInitiatedConnection(boolean sourceInitiatedConnection) {
	    this.sourceInitiatedConnection = sourceInitiatedConnection;
	}

	public void setDestination(short destination) {
	    this.destination = destination;
	}

	public void setSource(short source) {
	    this.source = source;
	}

	public void setTryingToEstablish(boolean isTryingToEstablish) {
	    this.isTryingToEstablish = isTryingToEstablish;
	}

	public void setAboutToEstablish(boolean isAboutToEstablish) {
	    this.isAboutToEstablish = isAboutToEstablish;
	}

	public void setEstablished(boolean isEstablished) {
	    this.isEstablished = isEstablished;
	}

	public void setAllowedToSendData(boolean isAllowedToSendData) {
	    this.isAllowedToSendData = isAllowedToSendData;
	}
}