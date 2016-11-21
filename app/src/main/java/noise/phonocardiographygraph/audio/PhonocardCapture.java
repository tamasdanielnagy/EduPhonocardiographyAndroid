package noise.phonocardiographygraph.audio;

import android.media.AudioRecord;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import noise.phonocardiographygraph.signal.ByteArray;
import noise.phonocardiographygraph.signal.SignalD;

/**
 * Created by Tamas on 2015.03.12..
 */
public class PhonocardCapture {

    public int sampleRate = 11025;

    public static final int AVERAGE_N = 48;
    public int frameSize = 2;
    public static final int DEFAULT_FRAME_SIZE = 2;
    public int channels = 1;
    public static final int USED_CHANNEL = 0;

    private SignalD signal;
    private boolean negateSignal = false;

    AudioRecord recorder;
    BufferedOutputStream tempOs;
    File tempFile = null;
    File dir;


    public PhonocardCapture(SignalD signal) {
        this.signal = signal;
    }



    public void readWavFile(InputStream is) throws IOException {
            BufferedInputStream in = new BufferedInputStream(is);

            byte[] intbuff = new byte[4];
            byte[] shortbuff = new byte[2];
            byte[] buff = null;

            boolean dataFound = false;
            String chunk;

            int j = 0;
            while (!dataFound && (in.available() > 0)) {
                in.read(intbuff, 0, 4);
                chunk = ByteArray.byteArrayToString(intbuff);
                System.out.println(chunk);
                if (chunk.equals("RIFF")) {
                    in.read(intbuff, 0, 4); // how big is the rest of this file
                } else if (chunk.equals("WAVE")) {
                    // do nothing, read next chunk
                } else if (chunk.equals("fmtSPACE")) {
                    in.read(intbuff, 0, 4); // size of this chunk
                    int chunkSize =  ByteArray.byteArrayToInt(intbuff, false);
                    in.read(shortbuff, 0, 2); // format code
                    if (ByteArray.byteArrayToInt(shortbuff,false) != 0x0001)
                        throw (new IOException("Only PCM encoding is supported."));
                    in.read(shortbuff, 0, 2); // number of channels
                    channels =  ByteArray.byteArrayToInt(shortbuff, false);
                    in.read(intbuff, 0, 4); // sample rate
                    sampleRate =  ByteArray.byteArrayToInt(intbuff, false);
                    in.read(intbuff, 0, 4);       // bytes per second
                    in.read(shortbuff, 0, 2);     // # of bytes in one sample, for all channels
                    in.read(shortbuff, 0, 2);     // how many bits in a sample(number)?  usually 16 or 24
                    int bitsPerSample = ByteArray.byteArrayToInt(shortbuff, false);
                    if (bitsPerSample <= 32) {
                        frameSize = bitsPerSample / 8;
                    } else {
                        frameSize = DEFAULT_FRAME_SIZE;
                    }
                    if (chunkSize > 16) //read extension
                        for (int i = 16; i < chunkSize; i+=2)
                            in.read(shortbuff, 0, 2);

                } else if (chunk.equals("fact")) {
                    in.read(intbuff, 0, 4); // size of this chunk
                    int chunkSize = ByteArray.byteArrayToInt(intbuff, false);
                    for (int i = 0; i < chunkSize; i += 2)
                        in.read(shortbuff, 0, 2);
                } else if (chunk.equals("data")) {
                    in.read(intbuff, 0, 4);      // how big is this data chunk
                    int dataLen = ByteArray.byteArrayToInt(intbuff, false);
                    // the actual data itself - just a long string of numbers
                    buff = new byte[dataLen];
                    if(dataLen != in.available())
                        throw (new IOException("File length mismatch."));
                    in.read(buff, 0, buff.length);
                    dataFound = true;
                } else {
                    if (j == 0 || j == 1)
                        throw (new IOException("Only WAV files are supported." ));
                    else
                        throw (new IOException("Unknown chunk in file." ));

                }
                j++;

            }
            //in.close();
            if (buff != null)
                writeBufferAverageArrayToSignal(buff, AVERAGE_N);
            else
                throw (new IOException("Data chunk not found in file."));
    }

    public void writeBufferAverageArrayToSignal(byte[] buffer, int avgN) {
        synchronized (signal) {
           // signal.clear();
            synchronized (buffer) {
                // System.out.println("write buffer to signal, " + signal.size());
                Double[] avgs;
               // avgs = ByteArray.averageArrayDouble(buffer, frameSize, avgN, false);
                avgs = ByteArray.byteArrayToDoubleArrayChannelsAvg(buffer, frameSize, channels, USED_CHANNEL, avgN, false);
                signal.setDt((1.0 / sampleRate)*avgN);
                if (negateSignal)
                    for (int i = 0; i < avgs.length; i++)
                        signal.add( -avgs[i]);
                else
                    for (int i = 0; i < avgs.length; i++)
                        signal.add(avgs[i]);
            }
        }
    }
    public SignalD getSignal() {
        return signal;
    }

}
