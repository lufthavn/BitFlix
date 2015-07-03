package tests;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import files.FileInfo;
import files.TorrentFile;
import bencoding.BDictionary;
import bencoding.BElement;
import bencoding.BInteger;
import bencoding.BList;
import bencoding.BString;
import bencoding.Decoder;


public class BencodingTests {

	private String torrent;
	@Before
	public void setUp() throws Exception {
		URL res = this.getClass().getResource("../resources/BigBuckBunny.torrent");
		torrent = new URI(res.toExternalForm()).getPath();
		
	}
	
	@Test
	public void CanDecode() {
		Decoder decoder = new Decoder(torrent);
		ArrayList<BElement> elements = decoder.decode();
		BDictionary dictionary = (BDictionary) elements.get(0);
		BString url = (BString)dictionary.get("announce");
		
		BDictionary info = (BDictionary)dictionary.get("info");
		byte[] pieces = ((BString)info.get("pieces")).getBinaryData();
		
		
		assertTrue(0 < elements.size());
		assertEquals("udp://open.demonii.com:1337/announce", url.getValue());
		assertEquals(pieces.length, 50700);
		assertNotEquals(pieces[pieces.length - 1], 0x00);
	}
	
	@Test
	public void CanParsePrimaryTracker()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		
		assertEquals("udp://open.demonii.com:1337/announce", file.getPrimaryTracker());
	}
	
	@Test
	public void CanParseTrackerList()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		
		assertEquals("udp://open.demonii.com:1337/announce", file.getTrackerList().get(0));
	}
	
	@Test
	public void CanParseFileInfo()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		
		List<FileInfo> files = file.getFiles();
		assertEquals(4, files.size());
		
		assertEquals("Content/big_buck_bunny_720p_surround.avi", files.get(0).getPath());
		assertEquals("Description.txt", files.get(1).getPath());
		assertEquals("LegalTorrents.txt", files.get(2).getPath());
		assertEquals("License.txt", files.get(3).getPath());
		assertEquals(332243668, files.get(0).getSize());
	}
	
	@Test
	public void CanCalculateInfoHash()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		
		byte[] expected = DatatypeConverter.parseHexBinary("0E876CE2A1A504F849CA72A5E2BC07347B3BC957");
		assertArrayEquals(expected, file.getInfoHash());
	}

	@Test
	public void CanParseBaseDirectory()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		
		assertEquals("Blender_Foundation_-_Big_Buck_Bunny_720p", file.getName());
	}
	
	@Test
	public void CanParsePieceInfo()
	{
		Decoder decoder = new Decoder(torrent);
		TorrentFile file = new TorrentFile(decoder);
		byte[] fexpected = DatatypeConverter.parseHexBinary("B46B5D307F804F9E0BFA854A2729720E7004E118");
		byte[] lexpected = DatatypeConverter.parseHexBinary("4C627911831EBC4C2A1B85944A93455FF5B435D1");
		assertEquals(131072, file.getPieceLength());
		assertArrayEquals(fexpected, file.getPieces()[0]);
		assertArrayEquals(lexpected, file.getPieces()[file.getPieces().length - 1]);
		assertEquals(2535, file.getPieces().length);
	}
	
	@Test
	public void canInterpretSingleFileMode() throws UnsupportedEncodingException{
		Decoder decoder = mock(Decoder.class);
		BDictionary d = new BDictionary();
		BDictionary info = new BDictionary();
		info.put("name", new BString("totally legal file.mp3".getBytes("UTF-8")));
		info.put("length", new BInteger(255));
		d.put("info", info);
		ArrayList<BElement> elements = new ArrayList<>();
		elements.add(d);
		when(decoder.decode()).thenReturn(elements);
		
		TorrentFile file = new TorrentFile(decoder);
		
		assertTrue(file.isSingleFile());
		assertEquals(255, file.getLength());
		assertEquals("totally legal file.mp3", file.getName());
		assertEquals(null, file.getFiles());
	}
	
	@Test
	public void canInterpretMultiFileMode() throws UnsupportedEncodingException
	{
		Decoder decoder = mock(Decoder.class);
		BDictionary d = new BDictionary();
		BDictionary info = new BDictionary();
		BList fileInfoList = new BList();
		
		BDictionary file1 = new BDictionary();
		file1.put("length", new BInteger(200));
		BList file1Path = new BList();
		file1Path.add(new BString("path1".getBytes()));
		file1Path.add(new BString("path2".getBytes()));
		file1.put("path", file1Path);
		
		BDictionary file2 = new BDictionary();
		file2.put("length", new BInteger(200));
		BList file2Path = new BList();
		file2Path.add(new BString("path3".getBytes()));
		file2Path.add(new BString("path4".getBytes()));
		file2.put("path", file2Path);
		
		fileInfoList.add(file1);
		fileInfoList.add(file2);
		info.put("files", fileInfoList);
		d.put("info", info);
		ArrayList<BElement> elements = new ArrayList<>();
		elements.add(d);
		when(decoder.decode()).thenReturn(elements);
		
		TorrentFile file = new TorrentFile(decoder);
		
		assertFalse(file.isSingleFile());
		assertEquals(2, file.getFiles().size());
		assertEquals("path3/path4", file.getFiles().get(1).getPath());
		assertEquals(-1, file.getLength());
	}
}
