/*
 * 11/19/04		1.0 moved to LGPL.
 * 
 * 06/04/01		Streaming support added. javalayer@javazoom.net
 * 
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;

/**
 * The <code>MP3Codec</code> class implements a simple command-line player for
 * MPEG audio files.
 *
 * @author Mat McGowan (mdm@techie.com)
 */
public class MP3Codec {

	private static final int samplesForLayerI = 384;
	private static final int samplesForLayerIII = 1152;

	private boolean remote = false;
	private static MP3Codec mp3Codec = null;
	private Player player = null;
	private RandomAccessFile accessor = null;
	private BufferedInputStream in;
	private Header header = null;
	private int samplesPerFrame;
	private int totalFrames;
	private byte[] samples;
	private SampleBuffer buffer;

	/**
	 * Create An instance of MP3 in singleton pattern.
	 * 
	 * @param songPath
	 * @return an instance of MP3, namely mp3Codec.
	 * @throws IOException
	 * @throws JavaLayerException
	 */
	public static MP3Codec createInstance(Path filePath) throws IOException,
			JavaLayerException {
		if (mp3Codec == null) {
			mp3Codec = new MP3Codec();
		}
		return mp3Codec.initialize(filePath);
	}

	/**
	 * Get current position in audio data, in milliseconds.
	 * 
	 * @return
	 */

	public long getPosition() {
		return mp3Codec.player.getCurrentPosition();
	}

	/**
	 * Get current Player instance of MP3 instance.
	 * 
	 * @return
	 */

	public Player getPlayerInstance() {
		return mp3Codec.player;
	}

	/**
	 * Cause current active SourceDataLine and input stream to close.
	 * 
	 * @throws BitstreamException
	 */

	public void closeStreaming() throws BitstreamException {
		mp3Codec.player.closeStreaming();
	}

	/**
	 * Decode a single frame header and initialize the SourceDataLine.
	 * 
	 * @return
	 * @throws BitstreamException
	 * @throws LineUnavailableException
	 */

	public Boolean decodeFrameHeader() throws BitstreamException,
			LineUnavailableException {
		return Player.decodeFrameHeander();
	}

	private MP3Codec() {
	}

	/**
	 * Initialize attributes of mp3Codec if it is not null.
	 */
	private MP3Codec initialize(Path filePath) {
		if (!mp3Codec.parsePath(filePath))
			mp3Codec = null;
		else {
			try {
				if (mp3Codec.remote == true)
					mp3Codec.getURLInputStream(filePath);
				else {
					mp3Codec.initializeByteStreams(filePath);
					mp3Codec.player = new Player(mp3Codec.in);
				}
			} catch (IOException | JavaLayerException e) {
				e.printStackTrace();
			}
		}
		return mp3Codec;
	}

	/**
	 * Get total duration of current audio data if song path exists.
	 * 
	 * @param songPath
	 * @return the total duration
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public float getTotoalDuration(Path filePath) throws IOException {
		return mp3Codec.player.getTotalDuration((int) mp3Codec.accessor
				.length());
	}

	/**
	 * set the filePath as given file path if file exists.
	 * 
	 * @param path
	 * @return
	 */
	protected boolean parsePath(Path path) {
		boolean parsed = false;
		if (Files.exists(path)) {
			parsed = true;
			remote = false;
		} else {
			JOptionPane.showMessageDialog(null, "File doesn't exist!",
					"I/O Error(s) Occurred!", JOptionPane.ERROR_MESSAGE);
			// showUsage();
		}
		return parsed;
	}

	public void showUsage() {
		System.out.println("Usage: jlp [-url] <filename>");
		System.out.println("");
		System.out.println(" e.g. : java javazoom.jl.player.jlp localfile.mp3");
		System.out
				.println("        java javazoom.jl.player.jlp -url http://www.server.com/remotefile.mp3");
		System.out
				.println("        java javazoom.jl.player.jlp -url http://www.shoutcastserver.com:8000");
	}

	/**
	 * Decode and play next single frame.
	 * 
	 * @return
	 * @throws JavaLayerException
	 * @throws LineUnavailableException
	 */
	public Boolean play() throws JavaLayerException, LineUnavailableException {
		return Player.decodeFrame();
	}

	/**
	 * Playing file from URL (Streaming).
	 * 
	 * @throws IOException
	 */
	protected void getURLInputStream(Path filePath) throws IOException {

		URL url = new URL(filePath.toAbsolutePath().toString());
		InputStream fin = url.openStream();
		in = new BufferedInputStream(fin);
	}

	/**
	 * Initialize byte streams from given file path.
	 */
	protected void initializeByteStreams(Path filePath) throws IOException {
		if (mp3Codec.accessor != null) {
			mp3Codec.accessor.close();
			mp3Codec.accessor = null;
		}
		// Construct a new input stream based on the RandomAccessFile in respect
		// to given file path.
		mp3Codec.accessor = new RandomAccessFile(filePath.toFile(), "r");
		mp3Codec.in = new BufferedInputStream(new InputStream() {

			@Override
			public int read() throws IOException {
				return mp3Codec.accessor.read();
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return mp3Codec.accessor.read(b, off, len);
			}
		});
	}

	protected AudioDevice getAudioDevice() throws JavaLayerException {
		return FactoryRegistry.systemRegistry().createAudioDevice();
	}

	/**
	 * Seek to given byte position, and initialize input stream
	 * 
	 * @param pos
	 * @throws IOException
	 * @throws JavaLayerException
	 */
	private void seekTo(long pos) throws IOException, JavaLayerException {
		if (mp3Codec == null || accessor == null) {
			return;
		}
		mp3Codec.accessor.seek(pos);
		in = new BufferedInputStream(new InputStream() {

			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				return mp3Codec.accessor.read();
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return mp3Codec.accessor.read(b, off, len);
			}
		});
		mp3Codec.player = new Player(in);
	}

	/**
	 * Initialize current Header instance with that of Class Player's.
	 * 
	 * @throws IOException
	 */
	private void getHeader() throws IOException {
		mp3Codec.header = mp3Codec.player.getHeader();
		mp3Codec.samplesPerFrame = header.layer() == 1 ? samplesForLayerI
				: samplesForLayerIII;
		mp3Codec.totalFrames = mp3Codec.header
				.max_number_of_frames((int) mp3Codec.accessor.length());
	}

	/**
	 * Initialize bitStream with Player's BitStream instance.
	 * 
	 * @return
	 * @throws BitstreamException
	 * @throws DecoderException
	 */
	private SampleBuffer getSampleBuffer() throws DecoderException,
			BitstreamException {
		return mp3Codec.player.getSampleBuffer();
	}

	/**
	 * Seek to the requested position and return the approximate audio position
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws JavaLayerException
	 * @throws LineUnavailableException
	 */
	public double readNextDecodebaleFrame(double request) throws IOException,
			JavaLayerException, LineUnavailableException {
		mp3Codec.player.getSourceDataLine().close();
		if (mp3Codec == null) {
			return 0;
		}
		mp3Codec.getHeader();

		long samplePos = Math.round(request * mp3Codec.samplesPerFrame
				* mp3Codec.totalFrames);
		// Get number of frames ahead of the request position
		int numOfFramesAhead = (int) (mp3Codec.totalFrames * request);
		int remainderSamples = (int) (samplePos - numOfFramesAhead
				* mp3Codec.samplesPerFrame);
		numOfFramesAhead += remainderSamples / mp3Codec.samplesPerFrame;
		remainderSamples = remainderSamples % mp3Codec.samplesPerFrame;
		double result = (remainderSamples == 0 ? 1e0 * numOfFramesAhead : 1e0
				* remainderSamples / mp3Codec.header.calculate_framesize()
				+ numOfFramesAhead)
				* mp3Codec.samplesPerFrame
				/ (mp3Codec.header.frequency() * 1e-3);

		mp3Codec.seekTo(numOfFramesAhead
				* mp3Codec.header.calculate_framesize());
		if (remainderSamples > 0) {
			mp3Codec.buffer = mp3Codec.getSampleBuffer();
			if (mp3Codec.buffer != null) {
				mp3Codec.samples = mp3Codec.player
						.getJavaSoundAudioDevice()
						.toByteArray(
								buffer.getBuffer(),
								remainderSamples,
								(buffer.getBufferLength() - remainderSamples) % 2 == 0 ? (buffer
										.getBufferLength() - remainderSamples)
										: (buffer.getBufferLength()
												- remainderSamples - 1));
				mp3Codec.player.getSourceDataLine().write(samples, 0,
						samples.length);
				synchronized (mp3Codec) {
					mp3Codec.notify();
					mp3Codec.player.getSourceDataLine().close();
					mp3Codec.player.getSourceDataLine().open();
					mp3Codec.player.getSourceDataLine().start();
				}
			}
		} else {
			synchronized (mp3Codec) {
				mp3Codec.notify();
				mp3Codec.player.getSourceDataLine().open();
				mp3Codec.player.getSourceDataLine().start();
			}
		}
		return result;
	}
}
