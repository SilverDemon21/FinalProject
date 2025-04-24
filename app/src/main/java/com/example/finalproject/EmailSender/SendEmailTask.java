package com.example.finalproject.EmailSender;

import android.os.AsyncTask;

import javax.mail.MessagingException;

public class SendEmailTask extends AsyncTask<Void, Void, String> {
    private String userEmail = "danielserebro21@gmail.com"; // Your Gmail email
    private String userPassword = "ount mfex whbt hciu"; // Your App Password
    private String recipient;
    private String subject;
    private String body;

    public SendEmailTask(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            GMailSender sender = new GMailSender(userEmail, userPassword);
            sender.sendMail(recipient, subject, body);
            return "Email Sent Successfully!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println(result); // Handle UI update if needed
    }
}
