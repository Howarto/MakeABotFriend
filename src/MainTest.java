
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javaFlacEncoder.FLACStreamController;

import javax.sound.sampled.AudioFileFormat;
import javax.swing.plaf.SliderUI;

import javazoom.jl.player.Player;

import org.apache.commons.io.IOUtils;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.FlacEncoder;
import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;
import com.darkprograms.speech.recognizer.Recognizer.Languages;
import com.darkprograms.speech.synthesiser.SynthesiserV2;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;


public class MainTest {
	
    private static final Double TONO_HOMBRE = 0.0;	// El Intervalo para el tono es [0, 2]
    private static final Double TONO_MUJER = 1.0;
    private static final String APIKEY = "AIzaSyDpdR_JNRUKkx9ze5kZa4J740kr5UGPeko";
    private static final int ESPERA = 5000;	// milliseconds of wait + 1000(offset)
    
    private static File stream2file (InputStream in, String prefix, String suffix) throws IOException {
    	File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(in, out);
        return tempFile;
    }
	
    private static void talk(InputStream inputStream) {
        try{
        	File tmp = stream2file(inputStream, "tmp", ".mp3");
            FileInputStream fis = new FileInputStream(tmp.getAbsolutePath());
            Player playMP3 = new Player(fis);

            playMP3.play();

        }catch(Exception e){System.out.println(e);}
    }
    
    private static String grabacion_propia() throws Exception {
    	Microphone microphone = new Microphone(AudioFileFormat.Type.WAVE);
    	
    	File audioFile = File.createTempFile("audioFile", ".wave");
    	audioFile.deleteOnExit();
    	
    	try {
    		microphone.captureAudioToFile(audioFile);
    		Thread.sleep(ESPERA);
    		microphone.close();
    		
    	} catch (Exception e) {
    		// TODO: handle exception
    		System.out.println("Pifiada del micro " + e.getMessage());
    		microphone.close();
    	}
    	
    	GoogleResponse googleResponse = new GoogleResponse();
    	Recognizer recognizer = new Recognizer(Recognizer.Languages.ENGLISH_US, APIKEY);
    	
    	File audioFileToGoogle = File.createTempFile("audioFileTmpToGoogle", ".flac");
    	audioFileToGoogle.deleteOnExit();
    	
    	FlacEncoder flacEncoder = new FlacEncoder();
    	flacEncoder.convertWaveToFlac(audioFile, audioFileToGoogle);;
    	googleResponse = recognizer.getRecognizedDataForFlac(audioFileToGoogle);
    	
    	return googleResponse.getResponse();
    }
    
    public static void main(String[] args) throws Exception {
    	
    	SynthesiserV2 synthesiser = new SynthesiserV2(APIKEY);
    	synthesiser.setLanguage("en");
    	ChatterBotFactory factory = new ChatterBotFactory();

        ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
        ChatterBotSession bot1session = bot1.createSession();

//        ChatterBot bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
//        ChatterBotSession bot1session = bot1.createSession();

        InputStream inputStream;
        String conversa = "Hi";
        String miRespuesta = "";
        Boolean listen = false;
        while (true) {
        	
        	System.out.println("TALK NOW!");
        	miRespuesta = grabacion_propia();
        	if (miRespuesta == null) miRespuesta = "";
            System.out.println("Yo> " + miRespuesta);
            
            // Habrá que ajustar los mensajes para que funcione bien.
            if (miRespuesta.equals("stop Google")) listen = false;
            else if (miRespuesta.equals("Okay Google") || listen) {
            	listen = true;
            	conversa = bot1session.think(conversa);
            	System.out.println("bot1> " + conversa);
            	synthesiser.setPitch(TONO_MUJER);
            	inputStream = synthesiser.getMP3Data(conversa);
            	talk(inputStream);
            	conversa = bot1session.think(conversa);
            }
        }
    }
}
