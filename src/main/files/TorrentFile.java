package files;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import util.Hashing;
import bencoding.BDictionary;
import bencoding.BElement;
import bencoding.BInteger;
import bencoding.BList;
import bencoding.BString;
import bencoding.Decoder;

public class TorrentFile{
	
	private BDictionary decoded;
	
	private List<FileInfo> fileInfo;
	private List<String> trackerList;
	private String primaryTracker;
	private byte[] infoHash;

	private String name;
	private long length;

	private int pieceLength;
	private byte[][] pieces;
	
	public TorrentFile(Decoder decoder) {
		//Since a torrent file consists of a single top level dictionary, we'll fetch this.
		this.decoded = (BDictionary)decoder.decode().get(0);
	}
	
	public TorrentFile(String path) throws URISyntaxException{
		this(new Decoder(path));
	}
	
	public byte[] getInfoHash()
	{
		if(infoHash == null)
			infoHash = parseInfoHash();
		return infoHash;
	}
	
	private byte[] parseInfoHash()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		decoded.get("info").encode(stream);
		byte[] bytes = stream.toByteArray();
		return Hashing.Sha1Hash(bytes);
	}
	
	public String getPrimaryTracker()
	{
		if(primaryTracker == null)
			primaryTracker = parsePrimaryTracker();
		return primaryTracker;
	}
	
	public String parsePrimaryTracker()
	{
		BString tracker = (BString) decoded.get("announce");
		return tracker.getValue();
	}
	
	public List<String> getTrackerList()
	{
		if(trackerList == null)
			trackerList = parseTrackerList();
		return trackerList;
	}
	
	private ArrayList<String> parseTrackerList()
	{
		BList trackerList = (BList) decoded.get("announce-list");
		ArrayList<String> trackers = new ArrayList<String>();
		for(BElement element : trackerList)
		{
			BList trackerElement = (BList) element;
			for(BElement trackerString : trackerElement)
			{
				String tracker = ((BString)trackerString).getValue();
				trackers.add(tracker);
			}
		}
		return trackers;
	}
	
	/**
	 * @return info for every file this torrent consists of. Returns null if torrent is in single file mode.
	 */
	public List<FileInfo> getFiles()
	{
		if(fileInfo == null)
			fileInfo = parseFiles();
		return fileInfo;
	}
	
	private LinkedList<FileInfo> parseFiles()
	{
		LinkedList<FileInfo> files = null;
		BDictionary info = (BDictionary) decoded.get("info");
		BList filesList = (BList) info.get("files");
		
		if(filesList != null){
			files = new LinkedList<FileInfo>();
			for(BElement element : filesList)
			{
				BDictionary dic = (BDictionary)element;
				long length = ((BInteger)dic.get("length")).getValue();
				
				StringBuilder builder = new StringBuilder();
				BList dicList = (BList)dic.get("path");
				for(BElement pathElement : dicList)
				{
					BString path = (BString)pathElement;
					builder.append(path.getValue());
					builder.append("/");
				}
				//remove the trailing "/"
				builder.deleteCharAt(builder.length() - 1);
				files.add(new FileInfo(builder.toString(), length));
			}
		}
		return files;
	}
	


	/**
	 * @return in single file mode, the filename of the file, in multifile mode, the name of the base directory of the files to be downloaded,
	 */
	public String getName() {
		if(name == null)
			name = parseName();
		return name;
	}

	private String parseName() {
		ArrayList<FileInfo> files = new ArrayList<FileInfo>();
		BDictionary info = (BDictionary) decoded.get("info");
		BString name = (BString) info.get("name");
		return name.getValue();
	}
	
	/**
	 * @return in single file mode, returns the length of the file in bytes. Returns the total length of all files if torrent is in multi file mode
	 */
	public long getLength() {
		if(length == 0){
			if(isSingleFile()){
				length = parseLength();
			}else{
				for(FileInfo info : getFiles()){
					length += info.getSize();
				}
			}
		}
		return length;
	}
	
	

	private long parseLength() {
		long length = -1;
		BDictionary info = (BDictionary) decoded.get("info");
		BInteger bLength = (BInteger) info.get("length");
		if(bLength != null){
			length = bLength.getValue();
		}
		return length;
	}

	public int getPieceLength() {
		if(pieceLength == 0)
			pieceLength = parsePieceLength();
		return pieceLength;
	}

	private int parsePieceLength() {
		BDictionary info = (BDictionary) decoded.get("info");
		BInteger length = (BInteger) info.get("piece length");
		return (int) length.getValue();
	}

	public byte[][] getPieces() {
		if(pieces == null)
			pieces = parsePieces();
		return pieces;
	}

	private byte[][] parsePieces() {
		//TODO: parse the piece hashes
		BDictionary info = (BDictionary) decoded.get("info");
		byte[] pieces = ((BString) info.get("pieces")).getBinaryData();
		int length = pieces.length;
		int hashLength = 20;
		byte[][] toReturn = new byte[length / 20][20];
		
		int counter = 0;

		for (int i = 0; i < length - hashLength + 1; i += hashLength)
			toReturn[counter++] = Arrays.copyOfRange(pieces, i, i + hashLength);

		if (length % hashLength != 0)
			toReturn[counter] = Arrays.copyOfRange(pieces, length - length % hashLength, length);
		
		return toReturn;
	}

	public boolean isSingleFile() {
		return getFiles() == null;
	}

}
