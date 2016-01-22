package ar.com.fernandospr.wns.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Form;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.logging.Logger;

import ar.com.fernandospr.wns.model.WnsAbstractNotification;
import ar.com.fernandospr.wns.model.WnsBadge;
import ar.com.fernandospr.wns.model.WnsTile;
import ar.com.fernandospr.wns.model.WnsToast;
import ar.com.fernandospr.wns.model.types.WnsNotificationType;

public class LoggingFilter implements ClientRequestFilter {

	private static final Logger log = Logger.getLogger(LoggingFilter.class);

	private Marshaller badgeMarshaller, tileMarshaller, toastMarshaller;

	public LoggingFilter() {
		try {
			badgeMarshaller = JAXBContext.newInstance(WnsBadge.class).createMarshaller();
			tileMarshaller = JAXBContext.newInstance(WnsTile.class).createMarshaller();
			toastMarshaller = JAXBContext.newInstance(WnsToast.class).createMarshaller();
		} catch (Exception e) {
		}
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		log.info(requestContext.getMethod() + " " + requestContext.getUri().toString());

		Object entity = requestContext.getEntity();
		if (entity instanceof Form) {
			Form f = (Form) entity;
			log.info(f.asMap().toString());
		} else if (entity instanceof WnsAbstractNotification) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			WnsAbstractNotification n = (WnsAbstractNotification) entity;
			String type = n.getType();
			Marshaller m = null;

			if (type.equals(WnsNotificationType.BADGE)) {
				m = badgeMarshaller;
			} else if (type.equals(WnsNotificationType.TILE)) {
				m = tileMarshaller;
			} else if (type.equals(WnsNotificationType.TOAST)) {
				m = toastMarshaller;
			}

			try {
				m.marshal(entity, out);
			} catch (JAXBException e) {
				log.error("Error marshalling entity: " + entity);
			}

			log.info(out.toString());
			out.close();

		} else if (entity instanceof ByteArrayInputStream) {
			@SuppressWarnings("resource")
			ByteArrayInputStream in = (ByteArrayInputStream) entity;
			byte[] stream = new byte[in.available()];
			in.read(stream, 0, in.available());
			in.reset();
			log.info(new String(stream));
		}
	}
}
