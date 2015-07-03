package files;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TorrentFileWriter implements Closeable {

	private String baseDirectory;
	private final TorrentFile torrent;
	private final List<FileProperties> files;

	public TorrentFileWriter(String baseDir, TorrentFile torrent) throws FileNotFoundException {
		this.baseDirectory = baseDir;
		this.torrent = torrent;
		this.files = new ArrayList<FileProperties>();
		
		
		if(torrent.isSingleFile()){
			String t = String.format("%s/%s", baseDirectory, torrent.getName());
			FileProperties p = new FileProperties(t, torrent.getLength());
			files.add(p);
		}else{
			for(FileInfo info : torrent.getFiles()){
				String t = String.format("%s/%s/%s", baseDirectory, torrent.getName(), info.getPath());
				FileProperties p = new FileProperties(t, info.getSize());
				files.add(p);
			}
		}
	}

	public void reserve() throws IOException {
		if(torrent.isSingleFile()){
			reserveSingle();
		}else{
			reserveMultiple();
		}
	}
	
	private void reserveSingle() throws IOException{
		createEmptyFile(files.get(0));
	}
	
	private void reserveMultiple() throws IOException{
		for(FileProperties prop : files){
			createEmptyFile(prop);
		}
	}
	
	public void createEmptyFile(FileProperties prop) throws IOException{
		File file = new File(prop.getPath());
		file.getParentFile().mkdirs();
		RandomAccessFile raf = prop.getFile();
		raf.setLength(prop.length);
		raf.close();
	}
	
	public void writePiece(Piece p) throws IOException {
		int startIndex = torrent.getPieceLength() * p.getIndex();
		RandomAccessFile raf;
		if(torrent.isSingleFile()){
			raf = files.get(0).getFile();
			raf.seek(startIndex);
			raf.write(p.getBytes());
		}else{
			writeMultiple(p);
		}
	}
	
	private void writeMultiple(Piece p) throws IOException {
		int startIndex = torrent.getPieceLength() * p.getIndex();
		int offset = 0;
		Iterator<FileProperties> iterator = files.iterator();
		FileProperties prop = iterator.next();
		while((prop.getLength() - 1) + offset < startIndex ){
			offset += prop.getLength();
			prop = iterator.next();
		}
		
		//piecelength = 40
		//startindex = 20
		//ingo.getSize = 30
		
		int fileIndex = files.indexOf(prop);

		//startindex is the position of the first byte to write to the file.
		startIndex = startIndex - offset;
		//if the total of the startindex and the length of the bytes to write is larger than the size of the target file, then the bytes need to be split
		//up and written across the following files.
		if((startIndex + p.getBytes().length) > prop.getLength()){
			//the rest is the amount of bytes left to be written.
			long rest = p.getBytes().length;
			//theBytes are the actual bytes to be written. Will be modified along the way.
			byte[] theBytes = p.getBytes();
			
			//when rest is 0, there are no more bytes left to write.
			while(rest > 0){
				//get the FileInfo of the corresponding file.
				prop = files.get(fileIndex);
				//subtracting the size of the file from the total of startIndex and the length of the bytes will give the amount
				//of remaining bytes.
				rest = (startIndex + theBytes.length) - prop.getLength();
				//calculate how big the subsection of bytes is.
				long to = theBytes.length;
				if(rest > 0){
					to -= rest;
				}
				//Get the subsection of theBytes that we need to write.
				byte[] dataToWrite = Arrays.copyOfRange(theBytes, 0, (int) to);
				//remove the subsection from theBytes, so it only contains the remaining bytes.
				theBytes = Arrays.copyOfRange(theBytes, (int) to, theBytes.length);
				//Write the bytes.
				writeBytes(dataToWrite, prop, startIndex);
				//set the startIndex to 0, since all the subsequent files after the first one,
				//will need to have their bytes written from the start.
				startIndex = 0;
				//increment the fileIndex, so the next iteration of the loop will write to the next file.
				fileIndex++;
			}
			
		}else{
			writeBytes(p.getBytes(), prop, startIndex);
		}
	}
	
	private void writeBytes(byte[] data, FileProperties prop,int startIndex) throws IOException{
		RandomAccessFile raf = prop.getFile();
		raf.seek(startIndex);
		raf.write(data);
	}

	@Override
	public void close() throws IOException {
		for(FileProperties stream : files){
				stream.close();
		}	
	}
	
	private class FileProperties implements Closeable{
		private RandomAccessFile file;
		private String path;
		private long length;
		
		public FileProperties(String path, long length){
			this.path = path;
			this.length = length;
		}
		
		/**
		 * @return the path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @return the length
		 */
		public long getLength() {
			return length;
		}

		public RandomAccessFile getFile() throws IOException{
			if(file == null || !file.getFD().valid()){
				file = new RandomAccessFile(path, "rw");
			}
			return file;
		}

		@Override
		public void close() throws IOException {
			if(file != null){
				file.close();
			}
			
		}
	}

}
