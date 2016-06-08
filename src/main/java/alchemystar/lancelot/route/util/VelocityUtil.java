/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.route.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VelocityUtil
 *
 * @Author lizhuyang
 */
public class VelocityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VelocityUtil.class);

    private static DateUtil dateUtil = new DateUtil();
    private static StringUtil stringUtil = new StringUtil();

    public static VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put("dateUtil", dateUtil);
        context.put("stringUtil", stringUtil);
        return context;
    }

    public static void main(String[] args) throws IOException {

        String tpl = ""//" #set($db_flag=$!stringUtil.crc32($F_CERTIFICATE_CODE))\r\n"
                + "$!stringUtil.substring($F_CERTIFICATE_CODE,-1)"+"_"+"_";
        Writer writer = new StringWriter();
        try {
            VelocityContext context = getContext();
            context.put("F_CERTIFICATE_CODE", "123123123123123456");
            Velocity.evaluate(context, writer, "", tpl);
            System.out.println(writer.toString());
        } catch (ParseErrorException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }

    }
}