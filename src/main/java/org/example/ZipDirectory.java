package org.example;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {

    public static void main(String[] args) {
        try {
            // Задаем путь к директории, которую нужно архивировать
            String filePath = "C://dev//m1-t6-jdk";
            // Задаем путь и имя архива, который будет создан
            String zipFile = "C://dev//m1-t6-jdk//zipFile.zip";

            String toEmail = "jjj@mail.ru";
            String fromEmail = "b.j@mail.ru";
            String password = "hg,jhg,lhgb.";
            String subject = "zipFile.zip";
            String body = "Привет, вот архив проекта.";

            // Создаем поток для записи архива в файл
            FileOutputStream fos = new FileOutputStream(zipFile);
            // Создаем поток для записи содержимого директории в архив
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Вызываем метод для архивации директории
            zipDirectory(filePath, filePath, zos);

            // Закрываем потоки
            zos.close();
            fos.close();

            sendEmail(toEmail, fromEmail, password, subject, body, zipFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Произошла ошибка при отправке почты: " + e.getMessage());
        }
    }


    private static void zipDirectory(String rootDir, String dir, ZipOutputStream zos) throws Exception {
        //получаем список всех файлов
        File[] files = new File(dir).listFiles();
        if (files != null) {
            for (var file : files) {
                if (file.isDirectory()) {
                    zipDirectory(rootDir, file.getAbsolutePath(), zos);
                } else {
                    String relativPath = file.getAbsolutePath().substring(rootDir.length() + 1);
                    var entry = new ZipEntry(relativPath);

                    zos.putNextEntry(entry);
                    try (var fis = new FileInputStream(file)) {
                        var buffer = new byte[1024];
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Произошла ошибка при чтении файла: " + e.getMessage());
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private static void sendEmail(String toEmail, String fromEmail, String password, String subject, String body,
                                  String attachmentPath) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.mail.ru");
        properties.put("mail.smtp.port", "587");

        // Создание сеанса для отправки почты с помощью заданных свойств и аутентификации
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(fromEmail, password);
            }
        });
        // Создание объекта сообщения электронной почты
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(fromEmail)); // Установка адреса отправителя
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // Установка адреса получателя
        message.setSubject(subject); // Установка темы письма

        // Создание MimeBodyPart для тела сообщения
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/plain");// Установка содержимого тела как простого текста

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(attachmentPath)));
        // Установка источника данных вложения из файла
        attachmentBodyPart.setFileName(MimeUtility.encodeText(new File(attachmentPath).getName())); // Установка источника данных вложения из файла

        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);

        Transport.send(message);

    }
}
