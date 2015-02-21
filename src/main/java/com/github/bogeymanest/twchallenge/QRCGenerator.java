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

    public static String stringToBytes(String str) throws IOException, WriterException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(str, BarcodeFormat.QR_CODE, 1250, 1250, hintMap);

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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
        ImageIO.write(image, "png", baos);
        baos.flush();
        String base64String = Base64.getEncoder().encodeToString(baos.toByteArray());
        baos.close();
        return base64String;
    }
}
