package nitezh.ministock.activities;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Test;

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


import nitezh.ministock.MimeSendTask;

/**
 * Created by raj34 on 2018-03-15.
 */
public class EmailTest {

    //Testing if an exception is thrown when an invalid email is provided by the user
    @Test
    public void TestInvalidEmail() {
        Properties props = new Properties();

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
        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(session);
            String fromAddress = "ministocks34@gmail.com";
            String toAddress = "InvalidEmail";
            //Setting sender address
            mm.setFrom(new InternetAddress(fromAddress));
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            //Adding subject
            mm.setSubject("Ministocks: Data CSV file Export");
            //Adding message
            mm.setText("You will find your requested data csv file attached to this email!", "utf-8", "html");

            //Sending email
            Transport.send(mm);
            //If this point is reached, it means no exception was thrown
            Assert.fail();
        }
        catch(MessagingException max){
            max.printStackTrace();
        }
    }

}