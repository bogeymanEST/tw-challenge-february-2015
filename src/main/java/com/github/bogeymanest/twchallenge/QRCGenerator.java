package com.github.bogeymanest.twchallenge;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import java.util.Base64;
import java.util.Hashtable;

public class QRCGenerator {

    public static String imgToBytes(File filepath) throws IOException {
        String dirName = "/Users/Joonas/My Documents/GitHub/tw-challenge-february-2015/out/";
        ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
        BufferedImage img=ImageIO.read(filepath);
        ImageIO.write(img, "png", baos);
        baos.flush();
        String base64String = Base64.getEncoder().encodeToString(baos.toByteArray());
        baos.close();
        return base64String;
    }

    public static void stringToImg(String string) throws IOException {
        byte[] bytearray = Base64.getDecoder().decode(string);
        BufferedImage imag = ImageIO.read(new ByteArrayInputStream(bytearray));
        ImageIO.write(imag, "png", new File("/Users/Joonas/My Documents/GitHub/tw-challenge-february-2015/out/","strToImg.png"));
    }


    public static void main(String[] args) {
        String myCodeText = "https://github.com/bogeymanEST/tw-challenge-february-2015";
        String filePath = "/Users/Joonas/My Documents/GitHub/tw-challenge-february-2015/out/QRCode.png";
        //lisada neutral kasutajanimi
        int size = 125;
        String fileType = "png";
        File myFile = new File(filePath);
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);

            int CrunchifyWidth = byteMatrix.getWidth();

            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
                    BufferedImage.TYPE_INT_RGB);
            //ImageIO.write(image, fileType, myFile);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, fileType, myFile);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //OutputStream b64 = new Base64.OutputStream(os);
            //Base64.getEncoder()
            //ImageIO.write(img, "png", b64);
            System.out.println(imgToBytes(myFile));
            //stringToImg(imgToBytes(myFile));

            String result = os.toString("UTF-8");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("\n\nYou have successfully created QR Code.");


    }
}
