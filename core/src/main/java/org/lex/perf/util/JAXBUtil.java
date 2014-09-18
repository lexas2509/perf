package org.lex.perf.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Created by Алексей on 18.09.2014.
 */
public class JAXBUtil {
    public static <T> T getObject(String contextPath, String name) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            JAXBElement<T> res = (JAXBElement<T>) unmarshaller.unmarshal(JAXBUtil.class.getClassLoader().getResource(name));
            return res.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
