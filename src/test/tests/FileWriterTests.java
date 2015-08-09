package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Test;
import org.apache.commons.io.*;

import files.FileInfo;
import files.Piece;
import files.TorrentFile;
import files.PieceWriter;

public class FileWriterTests {
	
	@Test
	public void canCreateEmptyFile() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile() + "/files";
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		when(torrent.getName()).thenReturn("legalfile.mp3");
		when(torrent.getLength()).thenReturn((long) 1024);
		when(torrent.isSingleFile()).thenReturn(true);
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		File file = new File(baseDir + "/legalfile.mp3");
		assertEquals(1024, file.length());
		writer.close();
	}
	
	@Test
	public void canCreateEmptyFiles() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.mp3", 1024));
		files.add(new FileInfo("file2/file2.mp4", 2048));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.isSingleFile()).thenReturn(false);
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		File file1 = new File(baseDir + "/files/file1/file1.mp3");
		File file2 = new File(baseDir + "/files/file2/file2.mp4");
		assertNotNull(file1);
		assertNotNull(file2);
		assertEquals(1024, file1.length());
		assertEquals(2048, file2.length());
		writer.close();
	}
	
	@Test
	public void canWritePieceInSingleFileMode() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile() + "/files";
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		when(torrent.getName()).thenReturn("test.txt");
		when(torrent.getLength()).thenReturn((long) 4);
		when(torrent.getPieceLength()).thenReturn(4);
		when(torrent.isSingleFile()).thenReturn(true);
		
		Piece p = mock(Piece.class);
		when(p.getIndex()).thenReturn(0);
		when(p.getBytes()).thenReturn("test".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p);
		File file = new File(baseDir + "/test.txt");
		String s = new String(Files.readAllBytes(file.toPath()));
		assertEquals("test", s);
		assertEquals(4, file.length());
		writer.close();
	}
	
	@Test
	public void canWriteOffsetPieceInSingleFileMode() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile() + "/files";
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		when(torrent.getName()).thenReturn("test.txt");
		when(torrent.getLength()).thenReturn((long) 8);
		when(torrent.getPieceLength()).thenReturn(4);
		when(torrent.isSingleFile()).thenReturn(true);
		
		Piece p = mock(Piece.class);
		when(p.getIndex()).thenReturn(1);
		when(p.getBytes()).thenReturn("test".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p);
		File file = new File(baseDir + "/test.txt");
		byte[] entireFile = Files.readAllBytes(file.toPath());
		String s = new String(Arrays.copyOfRange(entireFile, 4, 8));
		assertEquals("test", s);
		writer.close();
	}
	
	@Test
	public void canWritePieceInMultiFileMode() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.txt", 16));
		files.add(new FileInfo("file2/file2.txt", 32));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.getPieceLength()).thenReturn(4);
		when(torrent.isSingleFile()).thenReturn(false);
		
		Piece p = mock(Piece.class);
		when(p.getIndex()).thenReturn(4);
		when(p.getBytes()).thenReturn("test".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p);

		File file = new File(baseDir + "/files/file2/file2.txt");
		byte[] entireFile = Files.readAllBytes(file.toPath());
		String s = new String(Arrays.copyOfRange(entireFile, 0, 4));
		assertEquals("test", s);
		writer.close();
	}
	
	@Test
	public void canWritePieceAcrossTwoFiles() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.txt", 15));
		files.add(new FileInfo("file2/file2.txt", 32));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.getPieceLength()).thenReturn(10);
		when(torrent.isSingleFile()).thenReturn(false);
		
		Piece p = mock(Piece.class);
		when(p.getIndex()).thenReturn(1);
		when(p.getBytes()).thenReturn("test1test2".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p);

		File file1 = new File(baseDir + "/files/file1/file1.txt");
		File file2 = new File(baseDir + "/files/file2/file2.txt");
		byte[] entireFile1 = Files.readAllBytes(file1.toPath());
		byte[] entireFile2 = Files.readAllBytes(file2.toPath());
		String s1 = new String(Arrays.copyOfRange(entireFile1, 10, 15));
		String s2 = new String(Arrays.copyOfRange(entireFile2, 0, 5));
		System.out.println(new String(entireFile1));
		System.out.println(new String(entireFile2));
		writer.close();
	}
	
	@Test
	public void canWritePieceAcrossThreeFiles() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.txt", 10));
		files.add(new FileInfo("file2/file2.txt", 6));
		files.add(new FileInfo("file3/file3.txt", 11));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.getPieceLength()).thenReturn(26);
		when(torrent.isSingleFile()).thenReturn(false);
		
		Piece p = mock(Piece.class);
		when(p.getIndex()).thenReturn(0);
		when(p.getBytes()).thenReturn("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p);

		File file1 = new File(baseDir + "/files/file1/file1.txt");
		File file2 = new File(baseDir + "/files/file2/file2.txt");
		File file3 = new File(baseDir + "/files/file3/file3.txt");
		byte[] entireFile1 = Files.readAllBytes(file1.toPath());
		byte[] entireFile2 = Files.readAllBytes(file2.toPath());
		byte[] entireFile3 = Arrays.copyOfRange(Files.readAllBytes(file3.toPath()), 0, 10);
		String s1 = new String(entireFile1);
		String s2 = new String(entireFile2);
		String s3 = new String(entireFile3);
		assertEquals("ABCDEFGHIJ", s1);
		assertEquals("KLMNOP", s2);
		assertEquals("QRSTUVWXYZ", s3);
		writer.close();
	}
	
	@Test
	public void CanWriteMultiplePieces() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.txt", 10));
		files.add(new FileInfo("file2/file2.txt", 6));
		files.add(new FileInfo("file3/file3.txt", 11));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.getPieceLength()).thenReturn(13);
		when(torrent.isSingleFile()).thenReturn(false);
		
		Piece p1 = mock(Piece.class);
		when(p1.getIndex()).thenReturn(0);
		when(p1.getBytes()).thenReturn("ABCDEFGHIJKLM".getBytes());
		
		Piece p2 = mock(Piece.class);
		when(p2.getIndex()).thenReturn(1);
		when(p2.getBytes()).thenReturn("NOPQRSTUVWXYZ".getBytes());
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		writer.reserve();
		writer.writePiece(p1);
		writer.writePiece(p2);

		File file1 = new File(baseDir + "/files/file1/file1.txt");
		File file2 = new File(baseDir + "/files/file2/file2.txt");
		File file3 = new File(baseDir + "/files/file3/file3.txt");
		byte[] entireFile1 = Files.readAllBytes(file1.toPath());
		byte[] entireFile2 = Files.readAllBytes(file2.toPath());
		byte[] entireFile3 = Arrays.copyOfRange(Files.readAllBytes(file3.toPath()), 0, 10);
		String s1 = new String(entireFile1);
		String s2 = new String(entireFile2);
		String s3 = new String(entireFile3);
		assertEquals("ABCDEFGHIJ", s1);
		assertEquals("KLMNOP", s2);
		assertEquals("QRSTUVWXYZ", s3);
		writer.close();
	}
	
	@Test
	public void canReadPiece() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		
		File file1 = new File(baseDir + "/files/file1/file1.txt");
		File file2 = new File(baseDir + "/files/file2/file2.txt");
		File file3 = new File(baseDir + "/files/file3/file3.txt");
		
		file1.getParentFile().mkdirs();
		file1.createNewFile();
		file2.getParentFile().mkdirs();
		file2.createNewFile();
		file3.getParentFile().mkdirs();
		file3.createNewFile();
		
		Files.write(file1.toPath(), "ABCDEFGHIJLKMNOPQRSTUVWXYZ".getBytes());
		Files.write(file2.toPath(), "ABCDEFGHIJLKM".getBytes());
		Files.write(file3.toPath(), "NOPQRSTUVWXYZ".getBytes());
		
		TorrentFile torrent = mock(TorrentFile.class);
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		files.add(new FileInfo("file1/file1.txt", 26));
		files.add(new FileInfo("file2/file2.txt", 13));
		files.add(new FileInfo("file3/file3.txt", 13));
		when(torrent.getName()).thenReturn("files");
		when(torrent.getFiles()).thenReturn(files);
		when(torrent.getPieceLength()).thenReturn(20);
		when(torrent.isSingleFile()).thenReturn(false);
		
		PieceWriter writer = new PieceWriter(baseDir, torrent);
		
		byte[] block1 = writer.read(0, 0, 10);
		byte[] block2 = writer.read(1, 0, 12); 
		byte[] block3 = writer.read(1, 3, 19);
		byte[] block4 = writer.read(2, 0, 12);
		
		writer.close();
		assertArrayEquals("ABCDEFGHIJ".getBytes(), block1);
		assertArrayEquals("UVWXYZABCDEF".getBytes(), block2);
		assertArrayEquals("XYZABCDEFGHIJLKMNOP".getBytes(), block3);
		assertArrayEquals("OPQRSTUVWXYZ".getBytes(), block4);
	}
	
	@After
	public void tearDown() throws IOException{
		String baseDir = this.getClass().getResource("/").getFile();
		baseDir = URLDecoder.decode(baseDir, "utf-8");
		baseDir = new File(baseDir).getPath();
		File file = new File(baseDir + "/files");
		FileUtils.deleteDirectory(file);
	}
}
