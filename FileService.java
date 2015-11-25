public class FileService {

	public FileMsg handleRequest(FileMsg msg) {

		String filename = msg.getFilename();
		filename = "server_" + filename;
		msg.setFilename(filename);
		return msg;
	}
}
