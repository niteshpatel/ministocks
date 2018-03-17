package nitezh.ministock;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import nitezh.ministock.activities.GlobalWidgetData;
import nitezh.ministock.activities.widget.WidgetRow;

/**
 * Created by raj34 on 2018-03-14.
 */

public class MimeSendTask extends AsyncTask<Void, Void, Void> {
    private String toAddress;

    public MimeSendTask(String toAddress) {
        this.toAddress = toAddress;
    }

    @Override
    protected Void doInBackground(Void ... voids) {
        //Data Storage Directory
        String path =
                Environment.getExternalStorageDirectory() + File.separator  + "DataFolder";
        File folder = new File(path);
        folder.mkdirs();

        //file name
        File file = new File(folder, "config.csv");

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter outWriter = new OutputStreamWriter(fOut);

            List<WidgetRow> myList = GlobalWidgetData.getList();
            for (int i = 0; i< myList.size() ; i++)
            {
                outWriter.append(myList.get(i).getSymbol());
                outWriter.append("\n");
            }
            outWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        Uri fileuri = Uri.fromFile(file);


        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication("ministocks34@gmail.com", "ministocks123");
                    }
                });
Log.d("EMAIL", toAddress);
        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(session);
            String fromAddress = "ministocks34@gmail.com";

            //Setting sender address
            mm.setFrom(new InternetAddress(fromAddress));
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            //Adding subject
            mm.setSubject("Ministocks: Data CSV file Export");
            //Adding message
            mm.setText("You will find your requested data csv file attached to this email!", "utf-8", "html");

            mm.setDataHandler(new DataHandler(new ByteArrayDataSource(Files.toByteArray(file), "text/csv")));
            mm.setFileName("stocks.csv");
            //Sending email
            Transport.send(mm);
        }
        catch(MessagingException max){
            max.printStackTrace();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }
}
