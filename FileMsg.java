public class FileMsg {
	private boolean isGet;
	private String filename;
	private byte[] file;

	public FileMsg(boolean isGet, String filename, byte[] file) {
		this.isGet = isGet;
    this.filename = filename;
    this.file = file;
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

  public byte[] getFile() {
    return this.file;
  }

  public void setFile(byte[] file) {
    this.file = file;
  }
}
