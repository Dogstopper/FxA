public class FileMsg {
	private boolean isGet;
	private String filename;
	// private byte[] file;

	public FileMsg(boolean isGet, String filename) {
		isGet = this.isGet;
	}

	public boolean isGet() {
	    return this.isGet;
	}

	public String getFilename() {
	    return this.filename;
	}

	public void setGet(boolean isGet) {
	    this.isGet = isGet;
	}

	public void setFilename(String filename) {
	    this.filename = filename;
	}
}