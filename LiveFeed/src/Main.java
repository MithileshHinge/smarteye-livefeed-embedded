import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.video.ConverterFactory;

public class Main extends Thread {

	//private static final String outputFilename = "//home//nuc//Desktop//videos//";
	//private static final String outputFilename = "//home//odroid//Desktop//videos//";
	private static final String outputFilename = "C://Users//Home//Desktop//videos//";
	private static IMediaWriter writer;
	public static long startTime;
	public static Date dNow;
	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss_a");
	public static boolean writer_close = false;

	public static boolean startStore = false;
	public static boolean stopStore = false;
	public static boolean storing = false;
	
	public static String finalOutputFilename;
	public static String MailFilename;
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	/*static {
		System.loadLibrary("opencv_java248");
	}*/

	public static void main(String[] args) {
		VideoCapture cap = new VideoCapture(0);

		if (!cap.isOpened()) {
			System.out.println("Error - cannot open camera!");
			return;
		}

		Thread t = new StoreSignalThread();
		t.start();
		
		SendingFrame sendingFrame = new SendingFrame();
		sendingFrame.start();
		
		while (true) {

			if (startStore) {
				System.out.println("DEBUG 1 : storing started");
				dNow = new Date();
				finalOutputFilename = outputFilename + ft.format(dNow) + ".mp4"; 
				MailFilename = ft.format(dNow);
				writer = ToolFactory.makeWriter(finalOutputFilename);
				writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
				startTime = System.nanoTime();
				startStore = false;
				storing = true;
			}

			
			Mat camImage = new Mat();
			cap.read(camImage);

			if (camImage.empty()) {
				System.out.println(" --(!) No captured frame -- Break!");
				continue;
			}

			BufferedImage camimg = matToImage(camImage);
			BufferedImage image2 = ConverterFactory.convertToType(camimg, BufferedImage.TYPE_3BYTE_BGR);
			
			Mat camImgsmall = new Mat();
			Imgproc.pyrDown(camImage, camImgsmall);
			BufferedImage image3 = matToImage(camImgsmall);
			
			sendingFrame.frame = image3;
			
			// encode the image to stream #0
			
			if (storing)
				writer.encodeVideo(0, image2, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			// time4 = System.currentTimeMillis();

			camImage.release();

			if (stopStore) {
				writer.close();
				stopStore = false;
				storing = false;
				SendingVideo sendingVideo = new SendingVideo();
				sendingVideo.filepath = finalOutputFilename;
				sendingVideo.start();
				
				new Thread(new Runnable(){

					@Override
					public void run() {
						// Recipient's email ID needs to be mentioned.
					      String to = "isha.sagote@gmail.com";
					      // Sender's email ID needs to be mentioned
					      String from = "missblahboo@gmail.com";

					      final String username = "missblahboo@gmail.com";//change accordingly
					      final String password = "blahblahbooboo";//change accordingly
					      //final String video_file = "F://Video//";
					      // Assuming you are sending email through relay.jangosmtp.net
					      String host = "74.125.206.108";

					      Properties props = new Properties();
					      props.put("mail.smtp.auth", "true");
					      props.put("mail.smtp.starttls.enable", "true");
					      props.put("mail.smtp.host", host);
					      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					      props.setProperty("mail.smtp.socketFactory.fallback", "false");
					      props.setProperty("mail.smtp.port", "465");
					      props.setProperty("mail.smtp.socketFactory.port", "465");
					      
					      // Get the Session object.
					      Session session = Session.getInstance(props,
					         new javax.mail.Authenticator() {
					            protected PasswordAuthentication getPasswordAuthentication() {
					               return new PasswordAuthentication(username, password);
					            }
					         });
					      
					      try {
					          // Create a default MimeMessage object.
					          Message message = new MimeMessage(session);

					          // Set From: header field of the header.
					          message.setFrom(new InternetAddress(from));

					          // Set To: header field of the header.
					          message.setRecipients(Message.RecipientType.TO,
					             InternetAddress.parse(to));

					          // Set Subject: header field
					          message.setSubject("Magic Eye");

					          // Create the message part
					          BodyPart messageBodyPart = new MimeBodyPart();

					          // Now set the actual message
					          messageBodyPart.setText("Hello! This is a video recorded my your Magic Eye System on" + MailFilename);

					          // Create a multipart message
					          Multipart multipart = new MimeMultipart();

					          // Set text message part
					          multipart.addBodyPart(messageBodyPart);

					          // Part two is attachment
					          messageBodyPart = new MimeBodyPart();
					          DataSource source = new FileDataSource(finalOutputFilename);
					          finalOutputFilename = null;
					          messageBodyPart.setDataHandler(new DataHandler(source));
					          messageBodyPart.setFileName(MailFilename + ".mp4");
					          multipart.addBodyPart(messageBodyPart);

					          // Send the complete message parts
					          message.setContent(multipart);
					          System.out.println("reached jst b4 sending");

					          // Send message
					          Transport.send(message);
					          
					          System.out.println("Sent message successfully....");
					   
					       } catch (MessagingException e) {
					     	 System.out.println("Sending failed!!!");
					          throw new RuntimeException(e);
					       }
					}
				}).start();
			}
			
			

		}

	}

	public static BufferedImage matToImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}
}
