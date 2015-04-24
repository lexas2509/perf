package org.lex.perf.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;

/**
 * Created by Алексей on 18.09.2014.
 */
public class JAXBUtil {
    public static <T> T getObject(String contextPath, String name, java.lang.Class<T> cl) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            URL resource = JAXBUtil.class.getClassLoader().getResource(name);
            JAXBElement<T> res = (JAXBElement<T>) unmarshaller.unmarshal(resource);
            return res.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
