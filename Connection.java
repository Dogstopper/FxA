public class Connection {

	private short destination;
	private short source;

	private boolean isClient; // True if Host Initiated Connection

	private boolean isTryingToEstablish;
	private boolean isAboutToEstablish;
	private boolean isEstablished;
	private boolean isAllowedToSendData;
	private boolean isSendingData;

	private boolean isClientSentFIN;
	private boolean isServerSentACK;
	private boolean isServerSentFIN;
	private boolean isClientSentACK;

	public Connection(short destination, short source) {

		this.destination = destination;
		this.source = source;

		this.isClient = false;

		this.isTryingToEstablish = false;
		this.isAboutToEstablish = false;
		this.isEstablished = false;
		this.isAllowedToSendData = false;
		this.isSendingData = false;
	}

	public String connectionStateToString() {

		String state = this.source + "  -->  " + this.destination
					+ "\nisTryingToEstablish: " + (this.isTryingToEstablish ? "1" : "0")
					+ "\nisAboutToEstablish: " + (this.isAboutToEstablish ? "1" : "0")
					+ "\nisEstablished: " + (this.isEstablished ? "1" : "0")
					+ "\nisAllowedToSendData: " + (this.isAllowedToSendData ? "1" : "0")
					+ "\nisSendingData: " + (this.isSendingData ? "1" : "0");

		return state;
	}

	/* Setters and Getters */

	public boolean isClient() {
	    return this.isClient;
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

	public boolean isSendingData() {
	    return this.isSendingData;
	}


	public boolean isClientSentFIN() {
	    return this.isClientSentFIN;
	}

	public boolean isServerSentACK() {
	    return this.isServerSentACK;
	}

	public boolean isServerSentFIN() {
	    return this.isServerSentFIN;
	}

	public boolean isClientSentACK() {
	    return this.isClientSentACK;
	}

	public void setIsClient(boolean isClient) {
	    this.isClient = isClient;
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

	public void setSendingData(boolean isSendingData) {
	    this.isSendingData = isSendingData;
	}

	public void setClientSentFIN(boolean isClientSentFIN) {
	    this.isClientSentFIN = isClientSentFIN;
	}

	public void setServerSentACK(boolean isServerSentACK) {
	    this.isServerSentACK = isServerSentACK;
	}

	public void setServerSentFIN(boolean isServerSentFIN) {
	    this.isServerSentFIN = isServerSentFIN;
	}

	public void setClientSentACK(boolean isClientSentACK) {
	    this.isClientSentACK = isClientSentACK;
	}
}